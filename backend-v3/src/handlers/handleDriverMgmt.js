// ============================================================
// src/handlers/handleDriverMgmt.js
// Driver add, salary, advance, performance — FLEET feature
// ============================================================

'use strict';

const { addDriver, getUserDrivers, giveAdvance, calculateSalary } = require('../services/driver/driverService');
const { getDriverPerformance, formatPerformanceMessage } = require('../services/driver/performanceService');
const { createLogger } = require('../utils/logger');
const log = createLogger('handleDriverMgmt');

async function handleDriverMgmt(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // View all drivers
        if (text.includes('drivers') || text.includes('driver list') || text.includes('sab driver')) {
            return await viewDrivers(user);
        }

        // Add driver
        if (text.includes('add driver') || text.includes('naya driver') || text.includes('driver add')) {
            return getAddDriverGuide();
        }

        // Advance payment
        if (text.includes('advance') || text.includes('udhar')) {
            return await handleAdvance(user, message);
        }

        // Salary calculation
        if (text.includes('salary') || text.includes('tankhwah') || text.includes('payment')) {
            return await handleSalary(user, message);
        }

        // Performance
        if (text.includes('performance') || text.includes('report') || text.includes('kaam')) {
            return await handlePerformance(user, message);
        }

        return getDriverMenu();

    } catch (err) {
        log.error(`handleDriverMgmt error: ${err.message}`);
        return `Driver operation fail hua — dobara try karo`;
    }
}

async function viewDrivers(user) {
    const drivers = await getUserDrivers(user._id);

    if (!drivers.length) {
        return `👤 Koi driver registered nahi hai\n\n"driver add Ramesh 9876543210" likhke add karo`;
    }

    let msg = `👤 Tera Driver Team (${drivers.length})\n\n`;
    for (const d of drivers) {
        msg += `• ${d.name}`;
        if (d.phone) msg += ` — ${d.phone}`;
        if (d.assignedTruckId) msg += `\n  Truck: ${d.assignedTruckId.registrationNumber}`;
        msg += `\n  Salary: ₹${d.monthlySalary.toLocaleString('en-IN')}/month\n\n`;
    }
    return msg;
}

async function handleAdvance(user, message) {
    const amountMatch = message.match(/\d{3,}/);
    if (!amountMatch) {
        return (
            `💰 Driver ko advance dena hai?\n\n` +
            `Format: "advance Ramesh 5000"\n` +
            `Ya: "Ramesh ko 5000 advance diya"`
        );
    }

    const amount = parseInt(amountMatch[0]);
    return (
        `💰 ₹${amount.toLocaleString('en-IN')} advance note ho gaya\n\n` +
        `Salary calculation mein automatically deduct hoga\n` +
        `"salary" likhke check karo`
    );
}

async function handleSalary(user, message) {
    const drivers = await getUserDrivers(user._id);
    if (!drivers.length) return `Koi driver nahi hai — pehle add karo`;

    let msg = `💰 Salary Summary\n\n`;
    for (const d of drivers) {
        const sal = await calculateSalary(d._id);
        msg += `👤 ${sal.driver}\n`;
        msg += `  Monthly: ₹${sal.monthlySalary.toLocaleString('en-IN')}\n`;
        msg += `  Advance: ₹${sal.totalAdvance.toLocaleString('en-IN')}\n`;
        msg += `  Net Pay: ₹${sal.netSalary.toLocaleString('en-IN')}\n\n`;
    }
    return msg;
}

async function handlePerformance(user, message) {
    const drivers = await getUserDrivers(user._id);
    if (!drivers.length) return `Koi driver nahi hai`;

    const perf = await getDriverPerformance(drivers[0]._id, 'this_month');
    return formatPerformanceMessage(perf);
}

function getAddDriverGuide() {
    return (
        `👤 Naya Driver Add Karo\n\n` +
        `Format:\n` +
        `"driver add [Naam] [Phone]"\n\n` +
        `Example:\n` +
        `"driver add Ramesh Kumar 9876543210"`
    );
}

function getDriverMenu() {
    return (
        `👤 Driver Manager\n\n` +
        `• "drivers" — Sab drivers dekho\n` +
        `• "add driver" — Naya driver add\n` +
        `• "advance Ramesh 5000" — Advance do\n` +
        `• "salary" — Salary calculate karo\n` +
        `• "performance" — Driver ki performance`
    );
}

module.exports = { handleDriverMgmt };
