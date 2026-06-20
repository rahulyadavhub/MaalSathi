// ============================================================
// src/controllers/apiController.js
// Thin REST controllers wrapping existing services.
// No new business logic — services are the source of truth.
//
// FIELD NAME MISMATCHES (mobile ↔ backend):
//   mobile sends "freightAmount"  → service expects "freight"
//   mobile sends "advancePaid"    → service expects "advance"
//   mobile sends "cargoWeightTons" → service expects "cargoWeightTons" (match)
//   mobile sends "cargoType"      → service expects "cargoType" (match)
//   completeTrip service takes userId (finds active trip),
//     but mobile sends tripId — we look up the trip first.
// ============================================================

'use strict';

const mongoose = require('mongoose');
const User    = require('../models/User');
const Trip    = require('../models/Trip');
const tripService    = require('../services/trip/tripService');
const expenseService = require('../services/trip/expenseService');
const { TRIP_STATUS }   = require('../constants/tripStatus');
const { ALL_CATEGORIES } = require('../constants/expenseCategories');
const { createLogger }   = require('../utils/logger');
const log = createLogger('api');

function isValidObjectId(id) {
    return mongoose.Types.ObjectId.isValid(id);
}

function err(res, status, message, code) {
    return res.status(status).json({ error: message, code });
}

// ─── POST /api/users/register ────────────────────────────
async function registerUser(req, res) {
    try {
        const { phone, name, language, role, truckRegistration } = req.body;

        if (!phone || typeof phone !== 'string' || phone.length < 10) {
            return err(res, 400, 'Valid phone number required', 'INVALID_PHONE');
        }
        if (!name || typeof name !== 'string' || name.trim().length < 2) {
            return err(res, 400, 'Name required (min 2 chars)', 'INVALID_NAME');
        }

        let user = await User.findOne({ phone });
        const isNew = !user;

        if (isNew) {
            user = new User({ phone, name: name.trim() });
        } else {
            user.name = name.trim();
        }

        if (language && ['hindi', 'hinglish', 'english'].includes(language)) {
            user.language = language;
        }

        if (truckRegistration) {
            const reg = truckRegistration.toUpperCase().replace(/[\s\-]+/g, '');
            const existing = user.trucks.find(t => t.registration === reg);
            if (!existing) {
                user.trucks.push({ registration: reg, vehicleType: 'truck' });
            }
        }

        user.onboardingComplete = true;
        await user.save();

        log.info(`User ${isNew ? 'created' : 'updated'}: ${phone}`);
        res.status(isNew ? 201 : 200).json({ user, isNew });
    } catch (e) {
        log.error('registerUser failed: ' + e.message);
        err(res, 500, 'Registration failed', 'REGISTER_FAILED');
    }
}

// ─── POST /api/users/:userId/trips ───────────────────────
async function createTrip(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const { origin, destination, cargoType, cargoWeightTons, freightAmount, advancePaid } = req.body;
        if (!origin || !destination) {
            return err(res, 400, 'origin and destination required', 'MISSING_ROUTE');
        }

        const user = await User.findById(userId);
        if (!user) return err(res, 404, 'User not found', 'USER_NOT_FOUND');

        const truckReg = user.getPrimaryTruckReg();

        const parsedTrip = {
            origin:          origin.trim(),
            destination:     destination.trim(),
            cargoType:       cargoType || null,
            cargoWeightTons: cargoWeightTons || null,
            freight:         freightAmount || 0,
            advance:         advancePaid || 0,
        };

        let trip;
        if (parsedTrip.freight > 0) {
            trip = await tripService.createPending(userId, parsedTrip, truckReg);
            trip = await tripService.confirmTrip(trip._id);
        } else {
            trip = await tripService.createDraft(userId, parsedTrip);
        }

        log.info(`Trip created via API: ${trip._id}`);
        res.status(201).json(trip);
    } catch (e) {
        log.error('createTrip failed: ' + e.message);
        err(res, 500, 'Trip creation failed', 'CREATE_TRIP_FAILED');
    }
}

// ─── GET /api/users/:userId/trips ────────────────────────
async function listTrips(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const filter = { userId };
        if (req.query.status && Object.values(TRIP_STATUS).includes(req.query.status)) {
            filter.status = req.query.status;
        }

        const trips = await Trip.find(filter).sort({ startedAt: -1 }).limit(100).lean({ virtuals: true });
        res.json(trips);
    } catch (e) {
        log.error('listTrips failed: ' + e.message);
        err(res, 500, 'Failed to list trips', 'LIST_TRIPS_FAILED');
    }
}

// ─── GET /api/users/:userId/trips/active ─────────────────
async function getActiveTrip(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const trip = await tripService.getActiveTrip(userId);
        res.json(trip);
    } catch (e) {
        log.error('getActiveTrip failed: ' + e.message);
        err(res, 500, 'Failed to get active trip', 'GET_ACTIVE_FAILED');
    }
}

// ─── GET /api/trips/:tripId ──────────────────────────────
async function getTripById(req, res) {
    try {
        const { tripId } = req.params;
        if (!isValidObjectId(tripId)) return err(res, 400, 'Invalid tripId', 'INVALID_ID');

        const trip = await Trip.findById(tripId).lean({ virtuals: true });
        if (!trip) return err(res, 404, 'Trip not found', 'TRIP_NOT_FOUND');

        res.json(trip);
    } catch (e) {
        log.error('getTripById failed: ' + e.message);
        err(res, 500, 'Failed to get trip', 'GET_TRIP_FAILED');
    }
}

// ─── PUT /api/trips/:tripId/confirm ──────────────────────
async function confirmTrip(req, res) {
    try {
        const { tripId } = req.params;
        if (!isValidObjectId(tripId)) return err(res, 400, 'Invalid tripId', 'INVALID_ID');

        const trip = await tripService.confirmTrip(tripId);
        res.json(trip);
    } catch (e) {
        if (e.code === 'INVALID_TRANSITION') return err(res, 409, e.message, 'INVALID_TRANSITION');
        if (e.message === 'Trip not found') return err(res, 404, 'Trip not found', 'TRIP_NOT_FOUND');
        log.error('confirmTrip failed: ' + e.message);
        err(res, 500, 'Failed to confirm trip', 'CONFIRM_FAILED');
    }
}

// ─── PUT /api/trips/:tripId/complete ─────────────────────
// Note: tripService.completeTrip takes userId and finds the active trip.
// Mobile sends tripId, so we look up the trip to get userId.
async function completeTrip(req, res) {
    try {
        const { tripId } = req.params;
        if (!isValidObjectId(tripId)) return err(res, 400, 'Invalid tripId', 'INVALID_ID');

        const tripDoc = await Trip.findById(tripId);
        if (!tripDoc) return err(res, 404, 'Trip not found', 'TRIP_NOT_FOUND');
        if (tripDoc.status !== TRIP_STATUS.ACTIVE) {
            return err(res, 409, 'Trip is not active', 'INVALID_TRANSITION');
        }

        const completed = await tripService.completeTrip(tripDoc.userId);
        if (!completed) return err(res, 404, 'No active trip to complete', 'NO_ACTIVE_TRIP');

        res.json(completed);
    } catch (e) {
        if (e.code === 'INVALID_TRANSITION') return err(res, 409, e.message, 'INVALID_TRANSITION');
        log.error('completeTrip failed: ' + e.message);
        err(res, 500, 'Failed to complete trip', 'COMPLETE_FAILED');
    }
}

// ─── PUT /api/trips/:tripId/cancel ───────────────────────
async function cancelTrip(req, res) {
    try {
        const { tripId } = req.params;
        if (!isValidObjectId(tripId)) return err(res, 400, 'Invalid tripId', 'INVALID_ID');

        const trip = await tripService.cancelTrip(tripId);
        if (!trip) return err(res, 404, 'Trip not found', 'TRIP_NOT_FOUND');

        res.json(trip);
    } catch (e) {
        if (e.code === 'INVALID_TRANSITION') return err(res, 409, e.message, 'INVALID_TRANSITION');
        log.error('cancelTrip failed: ' + e.message);
        err(res, 500, 'Failed to cancel trip', 'CANCEL_FAILED');
    }
}

// ─── POST /api/users/:userId/expenses ────────────────────
async function addExpense(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const { category, amount, note, source, tripId } = req.body;

        if (!amount || Number(amount) <= 0) {
            return err(res, 400, 'amount must be > 0', 'INVALID_AMOUNT');
        }
        if (!category || !ALL_CATEGORIES.includes(category)) {
            return err(res, 400, 'Valid category required', 'INVALID_CATEGORY');
        }

        const result = await expenseService.logExpense(userId, {
            category,
            amount: Number(amount),
            note:   note || null,
            source: source || 'api',
        });

        res.status(201).json(result.expense);
    } catch (e) {
        log.error('addExpense failed: ' + e.message);
        err(res, 500, 'Failed to add expense', 'ADD_EXPENSE_FAILED');
    }
}

// ─── POST /api/users/:userId/expenses/batch ──────────────
async function addExpenseBatch(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const { expenses } = req.body;
        if (!Array.isArray(expenses) || expenses.length === 0) {
            return err(res, 400, 'expenses array required', 'INVALID_EXPENSES');
        }

        const result = await expenseService.logMultiple(userId, expenses, { source: 'api' });
        res.status(201).json(result.saved);
    } catch (e) {
        log.error('addExpenseBatch failed: ' + e.message);
        err(res, 500, 'Failed to add expenses', 'BATCH_EXPENSE_FAILED');
    }
}

// ─── GET /api/users/:userId/profit ───────────────────────
async function getProfit(req, res) {
    try {
        const { userId } = req.params;
        if (!isValidObjectId(userId)) return err(res, 400, 'Invalid userId', 'INVALID_ID');

        const period = req.query.period || 'today';
        const now = new Date();
        let startDate;

        switch (period) {
            case 'week': {
                const day = now.getDay();
                const diff = day === 0 ? 6 : day - 1;
                startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() - diff);
                break;
            }
            case 'month':
                startDate = new Date(now.getFullYear(), now.getMonth(), 1);
                break;
            case 'today':
            default:
                startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                break;
        }

        const result = await tripService.getProfitForPeriod(userId, startDate, now);

        res.json({
            period,
            revenue:   result.revenue,
            expenses:  result.expenses,
            profit:    result.netProfit,
            tripCount: result.tripCount,
        });
    } catch (e) {
        log.error('getProfit failed: ' + e.message);
        err(res, 500, 'Failed to get profit', 'PROFIT_FAILED');
    }
}

module.exports = {
    registerUser,
    createTrip,
    listTrips,
    getActiveTrip,
    getTripById,
    confirmTrip,
    completeTrip,
    cancelTrip,
    addExpense,
    addExpenseBatch,
    getProfit,
};
