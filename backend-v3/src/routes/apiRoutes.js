'use strict';

const { Router } = require('express');
const api = require('../controllers/apiController');

const router = Router();

// User
router.post('/users/register', api.registerUser);

// Trips — specific routes before parameterized ones
router.get('/users/:userId/trips/active',   api.getActiveTrip);
router.get('/users/:userId/trips',          api.listTrips);
router.post('/users/:userId/trips',         api.createTrip);
router.get('/trips/:tripId',                api.getTripById);
router.put('/trips/:tripId/confirm',        api.confirmTrip);
router.put('/trips/:tripId/complete',       api.completeTrip);
router.put('/trips/:tripId/cancel',         api.cancelTrip);

// Expenses — batch before single (both POST, different paths so order doesn't matter, but explicit)
router.post('/users/:userId/expenses/batch', api.addExpenseBatch);
router.post('/users/:userId/expenses',       api.addExpense);

// Profit
router.get('/users/:userId/profit', api.getProfit);

module.exports = router;
