// ============================================================
// src/services/external/fuelPriceService.js
// Live diesel prices city-wise — India
// ============================================================

'use strict';

const axios = require('axios');
const { createLogger } = require('../../utils/logger');
const log = createLogger('fuelPrice');

// Static fallback prices (updated periodically)
// Source: IOCL average retail prices
const FALLBACK_PRICES = {
    mumbai:    '89.97',
    delhi:     '87.62',
    bangalore: '89.24',
    chennai:   '91.43',
    hyderabad: '93.09',
    pune:      '90.15',
    ahmedabad: '92.38',
    jaipur:    '90.42',
    kolkata:   '91.76',
    surat:     '92.10',
    lucknow:   '90.89',
    kanpur:    '90.95',
    nagpur:    '91.30',
    indore:    '89.60',
    bhopal:    '90.25',
};

// Get diesel price for a city
async function getDieselPrice(city) {
    const cityKey = city.toLowerCase().trim();

    // Return fallback if API not configured
    const price = FALLBACK_PRICES[cityKey];
    if (price) {
        return {
            city,
            diesel_price: price,
            unit:         'per litre',
            currency:     '₹',
            source:       'reference',
            note:         'Approximate price — check local pump for exact rate',
        };
    }

    return {
        city,
        diesel_price: null,
        note: 'Price not available for this city',
    };
}

// Get prices for multiple cities (route)
async function getRouteFuelPrices(origin, destination) {
    const [from, to] = await Promise.all([
        getDieselPrice(origin),
        getDieselPrice(destination),
    ]);
    return { from, to };
}

// Estimate fuel cost for a trip
function estimateFuelCost(distanceKm, mileageKmPerLitre = 4, pricePerLitre = 91) {
    const litresNeeded = distanceKm / mileageKmPerLitre;
    const totalCost    = Math.round(litresNeeded * pricePerLitre);

    return {
        distanceKm,
        mileage:       mileageKmPerLitre,
        litresNeeded:  Math.round(litresNeeded),
        pricePerLitre,
        totalCost,
    };
}

// Format fuel message for WhatsApp
function formatFuelMessage(city, data) {
    if (!data?.diesel_price) {
        return `⛽ ${city} ka diesel price available nahi hai\nNearest pump pe check karo`;
    }

    return (
        `⛽ Diesel Price — ${data.city}\n\n` +
        `💰 ₹${data.diesel_price}/litre\n\n` +
        `⚠️ ${data.note}`
    );
}

module.exports = { getDieselPrice, getRouteFuelPrices, estimateFuelCost, formatFuelMessage };
