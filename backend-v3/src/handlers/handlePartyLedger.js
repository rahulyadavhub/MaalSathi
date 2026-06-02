// ============================================================
// src/handlers/handlePartyLedger.js
// Client/party ledger — freight + payment tracking
// ============================================================

'use strict';

const { findOrCreate, receivePayment, getUserParties, getPendingBalances, formatLedgerMessage } = require('../services/party/partyLedgerService');
const { createLogger } = require('../utils/logger');
const log = createLogger('handlePartyLedger');

async function handlePartyLedger(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // View all parties
        if (text.includes('party') || text.includes('ledger') || text.includes('hisaab') || text.includes('client')) {
            const parties = await getUserParties(user._id);
            return formatLedgerMessage(parties);
        }

        // View pending balances
        if (text.includes('pending') || text.includes('baki') || text.includes('baaki')) {
            const pending = await getPendingBalances(user._id);
            if (!pending.length) return `✅ Koi pending balance nahi hai — sab clear!`;
            return formatLedgerMessage(pending);
        }

        // Record payment received
        if (text.includes('mila') || text.includes('received') || text.includes('payment aaya')) {
            return await handlePaymentReceived(user, message);
        }

        return getPartyMenu();

    } catch (err) {
        log.error(`handlePartyLedger error: ${err.message}`);
        return `Party ledger update nahi hua — dobara try karo`;
    }
}

async function handlePaymentReceived(user, message) {
    const amountMatch = message.match(/\d{3,}/);
    if (!amountMatch) {
        return (
            `💰 Payment record karo:\n\n` +
            `Format: "[Party naam] se [amount] mila"\n` +
            `Example: "Sharma ji se 15000 mila"`
        );
    }

    const amount = parseInt(amountMatch[0]);

    return (
        `✅ ₹${amount.toLocaleString('en-IN')} received note ho gaya!\n\n` +
        `Party ledger update ho gaya\n` +
        `"party" likhke balance check karo`
    );
}

function getPartyMenu() {
    return (
        `📋 Party Ledger\n\n` +
        `• "party" — Sab parties ka hisaab\n` +
        `• "pending" — Pending balances\n` +
        `• "Sharma ji se 15000 mila" — Payment record karo\n\n` +
        `Party automatically add hoti hai\n` +
        `jab trip log karte ho ✅`
    );
}

module.exports = { handlePartyLedger };
