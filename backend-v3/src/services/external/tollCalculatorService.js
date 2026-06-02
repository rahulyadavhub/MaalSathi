// ============================================================
// src/services/external/tollCalculatorService.js
// Route toll estimation for Indian highways
// ============================================================

'use strict';

const axios = require('axios');
const { createLogger } = require('../../utils/logger');
const log = createLogger('tollCalc');

// Approximate toll rates per km on Indian highways (₹/km)
// Based on NHAI rates for 2-axle trucks
const TOLL_RATE_PER_KM = {
    national_highway: 2.5,
    state_highway:    1.8,
    expressway:       3.2,
    default:          2.0,
};

// Estimate toll for a route
function estimateToll(distanceKm, routeType = 'default') {
    const rate = TOLL_RATE_PER_KM[routeType] || TOLL_RATE_PER_KM.default;
    const estimatedToll = Math.round(distanceKm * rate);

    // Add plaza stops estimate (avg 1 plaza per 50km)
    const plazaCount = Math.floor(distanceKm / 50);

    return {
        distanceKm,
        routeType,
        estimatedToll,
        plazaCount,
        ratePerKm: rate,
        note: 'Approximate estimate — actual toll may vary',
    };
}

// Common Indian highway routes — pre-calculated estimates
const ROUTE_ESTIMATES = {
    'mumbai-pune':      { distance: 149, toll: 310,  plazas: 3 },
    'mumbai-nashik':    { distance: 167, toll: 280,  plazas: 3 },
    'mumbai-surat':     { distance: 280, toll: 520,  plazas: 5 },
    'delhi-agra':       { distance: 204, toll: 425,  plazas: 4 },
    'delhi-jaipur':     { distance: 281, toll: 580,  plazas: 5 },
    'delhi-chandigarh': { distance: 248, toll: 480,  plazas: 4 },
    'bangalore-mysore': { distance: 143, toll: 260,  plazas: 2 },
    'bangalore-chennai':{ distance: 346, toll: 680,  plazas: 6 },
    'hyderabad-pune':   { distance: 559, toll: 1050, plazas: 9 },
    'chennai-coimbatore':{ distance: 491, toll: 890, plazas: 8 },
};

// Get toll estimate for known route
function getRouteEstimate(origin, destination) {
    const key1 = `${origin.toLowerCase()}-${destination.toLowerCase()}`;
    const key2 = `${destination.toLowerCase()}-${origin.toLowerCase()}`;

    const data = ROUTE_ESTIMATES[key1] || ROUTE_ESTIMATES[key2];

    if (data) {
        return {
            origin,
            destination,
            distanceKm:    data.distance,
            estimatedToll: data.toll,
            plazaCount:    data.plazas,
            source:        'database',
            note:          'Estimate for 2-axle truck on NH',
        };
    }

    // Fallback — generic estimate
    return null;
}

// Format toll message for WhatsApp
function formatTollMessage(origin, destination, estimate) {
    if (!estimate) {
        return (
            `🛣️ Toll Calculator\n\n` +
            `Route: ${origin} → ${destination}\n\n` +
            `❌ Is route ka data available nahi hai\n` +
            `NHAI website check karo: tis.nhai.gov.in`
        );
    }

    return (
        `🛣️ Toll Estimate\n\n` +
        `📍 ${estimate.origin} → ${estimate.destination}\n` +
        `📏 Distance: ~${estimate.distanceKm} km\n` +
        `🚧 Plazas: ~${estimate.plazaCount}\n` +
        `💰 Toll: ~₹${estimate.estimatedToll}\n\n` +
        `⚠️ ${estimate.note}`
    );
}

module.exports = { estimateToll, getRouteEstimate, formatTollMessage };
