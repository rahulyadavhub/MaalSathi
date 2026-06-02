// ============================================================
// src/handlers/handleDocuments.js
// RC, Insurance, PUC, Permit store + view + expiry
// ============================================================

'use strict';

const { addDocument, getUserDocuments, getExpiringDocs, formatDocsMessage } = require('../services/document/documentService');
const { getStatusText, getEmoji } = require('../services/document/expiryChecker');
const { DOCUMENT_TYPE, DOCUMENT_LABELS } = require('../constants/documentTypes');
const { createLogger } = require('../utils/logger');
const log = createLogger('handleDocuments');

async function handleDocuments(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // View all documents
        if (text.includes('documents') || text.includes('docs') || text.includes('kagaz')) {
            const docs = await getUserDocuments(user._id);
            return formatDocsMessage(docs);
        }

        // View expiring documents
        if (text.includes('expire') || text.includes('expiry') || text.includes('khatam')) {
            const docs = await getExpiringDocs(user._id, 30);
            if (!docs.length) return `✅ Koi document 30 din mein expire nahi ho raha`;
            return formatDocsMessage(docs);
        }

        // Add RC
        if (text.includes('rc') || text.includes('registration')) {
            return getAddDocMessage('RC', 'RC');
        }

        // Add Insurance
        if (text.includes('insurance') || text.includes('bima')) {
            return getAddDocMessage('Insurance', 'Insurance');
        }

        // Add PUC
        if (text.includes('puc') || text.includes('pollution')) {
            return getAddDocMessage('PUC', 'PUC');
        }

        // Add Permit
        if (text.includes('permit')) {
            return getAddDocMessage('Permit', 'Permit');
        }

        // Add Fitness
        if (text.includes('fitness') || text.includes('fc')) {
            return getAddDocMessage('Fitness', 'Fitness Certificate');
        }

        // Parse date and save document
        const dateMatch = message.match(/(\d{1,2})[\/\-\.](\d{1,2})[\/\-\.](\d{2,4})/);
        if (dateMatch && user.convState?.pendingDocType) {
            return await saveDocument(user, dateMatch[0]);
        }

        // Default — show menu
        return getDocMenu();

    } catch (err) {
        log.error(`handleDocuments error: ${err.message}`);
        return `Document save nahi hua — dobara try karo`;
    }
}

async function saveDocument(user, dateStr, docType) {
    try {
        const [day, month, year] = dateStr.split(/[\/\-\.]/);
        const fullYear = year.length === 2 ? `20${year}` : year;
        const expiryDate = new Date(`${fullYear}-${month.padStart(2,'0')}-${day.padStart(2,'0')}`);

        if (isNaN(expiryDate)) return `❌ Galat date format — DD/MM/YYYY mein likho`;

        const doc = await addDocument(user._id, user.activeTruckId, docType, expiryDate);
        const status = getStatusText(expiryDate);
        const emoji  = getEmoji(doc ? 'valid' : 'expired');

        return (
            `${emoji} ${DOCUMENT_LABELS[docType]} save ho gaya!\n\n` +
            `📅 Expiry: ${expiryDate.toLocaleDateString('en-IN')}\n` +
            `⏳ Status: ${status}`
        );
    } catch (err) {
        log.error(`saveDocument error: ${err.message}`);
        return `Document save nahi hua`;
    }
}

function getAddDocMessage(type, label) {
    return (
        `📄 ${label} ki expiry date batao\n\n` +
        `Format: DD/MM/YYYY\n` +
        `Example: 15/06/2026`
    );
}

function getDocMenu() {
    return (
        `📄 Document Manager\n\n` +
        `Kya karna hai?\n\n` +
        `• "RC" — RC expiry add karo\n` +
        `• "Insurance" — Insurance expiry\n` +
        `• "PUC" — Pollution certificate\n` +
        `• "Permit" — Permit expiry\n` +
        `• "Fitness" — FC expiry\n` +
        `• "Documents" — Sab dekhlo\n` +
        `• "Expiry" — Expiring docs dekhlo`
    );
}

module.exports = { handleDocuments, saveDocument };
