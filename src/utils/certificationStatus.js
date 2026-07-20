// Matches the 30-day "expiring soon" threshold used by the backend's
// Notification module (see NotificationServiceImpl.syncCertificationNotifications)
// so the banners here agree with what triggers a notification/email.
const EXPIRING_SOON_DAYS = 30;

export function getCertificationStatus(cert) {
  if (!cert.expiryDate) return { key: 'NO_EXPIRY', label: 'No expiry', color: '#64748b', bg: '#f1f5f9' };

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const expiry = new Date(cert.expiryDate);
  const daysUntilExpiry = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24));

  if (daysUntilExpiry < 0) {
    return { key: 'EXPIRED', label: 'Expired', color: '#e11d48', bg: '#fff1f2', daysUntilExpiry };
  }
  if (daysUntilExpiry <= EXPIRING_SOON_DAYS) {
    return { key: 'EXPIRING_SOON', label: `Expires in ${daysUntilExpiry}d`, color: '#f59e0b', bg: '#fffbeb', daysUntilExpiry };
  }
  return { key: 'VALID', label: 'Valid', color: '#0d9488', bg: '#f0fdfa', daysUntilExpiry };
}

export function summarizeCertifications(certifications) {
  const statuses = certifications.map(getCertificationStatus);
  return {
    expired: statuses.filter((s) => s.key === 'EXPIRED').length,
    expiringSoon: statuses.filter((s) => s.key === 'EXPIRING_SOON').length,
  };
}
