// ============================================================
// src/handlers/handleMultiTruck.js
// Add/switch/view multiple trucks — FLEET feature
// ============================================================

'use strict';

const { addTruck, getUserTrucks, switchActiveTruck, getActiveTruck } = require('../services/truck/truckService');
const { getFleetOverview, formatFleetMessage } = require('../services/truck/fleetService');
const { createLogger } = require('../utils/logger');
const log = createLogger('handleMultiTruck');

async function handleMultiTruck(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // View all trucks
        if (text.includes('trucks') || text.includes('gaadi') || text.includes('fleet')) {
            return await viewTrucks(user);
        }

        // Add new truck
        if (text.includes('add') || text.includes('naya') || text.includes('new truck')) {
            return getAddTruckGuide();
        }

        // Switch active truck
        if (text.includes('switch') || text.includes('change truck') || text.includes('badlo')) {
            return await switchTruckMenu(user);
        }

        // Fleet view / analytics
        if (text.includes('fleet view') || text.includes('sabka') || text.includes('all trucks')) {
            const overview = await getFleetOverview(user._id, 'this_month');
            return formatFleetMessage(overview);
        }

        // Parse new truck registration number
        const regMatch = message.match(/[A-Z]{2}\s?\d{2}\s?[A-Z]{1,2}\s?\d{4}/i);
        if (regMatch) {
            return await addNewTruck(user, regMatch[0].toUpperCase());
        }

        return getTruckMenu();

    } catch (err) {
        log.error(`handleMultiTruck error: ${err.message}`);
        return `Truck operation fail hua — dobara try karo`;
    }
}

async function viewTrucks(user) {
    const trucks = await getUserTrucks(user._id);

    if (!trucks.length) {
        return `🚛 Koi truck registered nahi hai\n\n"truck add MH04AB1234" likhke add karo`;
    }

    let msg = `🚛 Tera Fleet (${trucks.length} trucks)\n\n`;
    for (const t of trucks) {
        const isActive = String(t._id) === String(user.activeTruckId);
        const star = isActive ? ' ⭐ (Active)' : '';
        msg += `${t.registrationNumber}${star}\n`;
        if (t.model) msg += `   Model: ${t.model}\n`;
        if (t.currentDriverId) msg += `   Driver: ${t.currentDriverId.name}\n`;
        msg += '\n';
    }

    msg += `Switch karne ke liye: "switch MH04AB1234"`;
    return msg;
}

async function switchTruckMenu(user) {
    const trucks = await getUserTrucks(user._id);
    if (trucks.length <= 1) return `Sirf ek truck hai — pehle aur add karo`;

    let msg = `🚛 Kaunsa truck select karna hai?\n\n`;
    trucks.forEach((t, i) => {
        msg += `${i + 1}. ${t.registrationNumber}`;
        if (String(t._id) === String(user.activeTruckId)) msg += ' ⭐';
        msg += '\n';
    });
    msg += `\nNumber bhejo (1, 2, 3...)`;
    return msg;
}

async function addNewTruck(user, regNumber) {
    const truck = await addTruck(user._id, { registrationNumber: regNumber });
    return (
        `✅ Truck add ho gaya!\n\n` +
        `🚛 ${truck.registrationNumber}\n\n` +
        `Model, capacity aur driver baad mein add kar sakte ho\n` +
        `"trucks" likhke sab dekho`
    );
}

function getAddTruckGuide() {
    return (
        `🚛 Naya Truck Add Karo\n\n` +
        `Registration number type karo:\n` +
        `Example: MH 04 AB 1234\n\n` +
        `Ya: "truck add MH04AB1234"`
    );
}

function getTruckMenu() {
    return (
        `🚛 Fleet Manager\n\n` +
        `• "trucks" — Sab trucks dekho\n` +
        `• "add truck" — Naya truck add karo\n` +
        `• "fleet view" — Combined analytics\n` +
        `• "switch" — Active truck badlo`
    );
}

module.exports = { handleMultiTruck };
