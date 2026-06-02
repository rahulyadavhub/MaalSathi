// ============================================================
// src/handlers/handleFuelTracker.js
// Fuel efficiency tracking + live diesel prices
// ============================================================

'use strict';

const { compareAllTrucks }               = require('../services/analytics/fuelEfficiency');
const { getDieselPrice, estimateFuelCost, formatFuelMessage } = require('../services/external/fuelPriceService');
const { createLogger }                   = require('../utils/logger');
const log = createLogger('handleFuelTracker');

async function handleFuelTracker(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // Live diesel price
        if (text.includes('price') || text.includes('rate') || text.includes('kitna hai')) {
            return await handleDieselPrice(message);
        }

        // Fuel efficiency
        if (text.includes('mileage') || text.includes('efficiency') || text.includes('km/l') || text.includes('average')) {
            return await handleEfficiency(user);
        }

        // Fuel cost estimate for a trip
        if (text.includes('kitna diesel') || text.includes('fuel cost') || text.includes('estimate')) {
            return handleEstimate(message);
        }

        return getFuelMenu();

    } catch (err) {
        log.error(`handleFuelTracker error: ${err.message}`);
        return `Fuel tracker error — dobara try karo`;
    }
}

async function handleDieselPrice(message) {
    // Extract city name from message
    const cities = ['mumbai', 'delhi', 'bangalore', 'chennai', 'hyderabad', 'pune',
                    'jaipur', 'ahmedabad', 'surat', 'kolkata', 'lucknow'];

    const found = cities.find(c => message.toLowerCase().includes(c));
    const city  = found ? found.charAt(0).toUpperCase() + found.slice(1) : 'Mumbai';

    const data = await getDieselPrice(city);
    return formatFuelMessage(city, data);
}

async function handleEfficiency(user) {
    try {
        const trucks = await compareAllTrucks(user._id, 'this_month');

        if (!trucks.length) {
            return `🚛 Koi truck data nahi hai abhi\nTrips aur expenses log karo pehle`;
        }

        let msg = `⛽ Fuel Efficiency — This Month\n\n`;
        for (const t of trucks) {
            const emoji = t.efficiency >= 4 ? '✅' : t.efficiency >= 3 ? '🟡' : '🔴';
            msg += `${t.truck}\n`;
            msg += `${emoji} ${t.efficiency} km/L\n`;
            msg += `   Diesel: ₹${t.totalCost.toLocaleString('en-IN')} (${t.totalLitres}L)\n\n`;
        }
        return msg;

    } catch (err) {
        return `Efficiency data available nahi hai`;
    }
}

function handleEstimate(message) {
    const distMatch = message.match(/\d{2,4}/);
    if (!distMatch) {
        return (
            `⛽ Fuel Cost Estimate\n\n` +
            `Distance type karo:\n` +
            `"300 km ka diesel kitna hoga"\n\n` +
            `Average 4 km/L aur ₹91/L se calculate hoga`
        );
    }

    const dist = parseInt(distMatch[0]);
    const est  = estimateFuelCost(dist);

    return (
        `⛽ Fuel Estimate — ${dist} km\n\n` +
        `🚛 Mileage: ${est.mileage} km/L\n` +
        `⛽ Diesel: ~${est.litresNeeded} litres\n` +
        `💰 Cost: ~₹${est.totalCost.toLocaleString('en-IN')}\n\n` +
        `⚠️ Actual amount vary kar sakta hai`
    );
}

function getFuelMenu() {
    return (
        `⛽ Fuel Tracker\n\n` +
        `• "diesel price Mumbai" — Live rate\n` +
        `• "mileage" — Fuel efficiency dekho\n` +
        `• "300 km diesel kitna" — Cost estimate\n\n` +
        `Diesel expenses log karte raho\n` +
        `efficiency automatically track hogi ✅`
    );
}

module.exports = { handleFuelTracker };
