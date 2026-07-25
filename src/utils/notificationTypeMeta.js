import { Bell, AlertTriangle, AlertOctagon, ShieldAlert, GraduationCap, CalendarClock, Sparkles, Trophy } from 'lucide-react';

export const TYPE_ICON = {
  CRITICAL_GAP: { Icon: AlertOctagon, color: '#e11d48', bg: '#fff1f2' },
  MODERATE_GAP: { Icon: AlertTriangle, color: '#f59e0b', bg: '#fffbeb' },
  CERTIFICATION_EXPIRING: { Icon: ShieldAlert, color: '#f59e0b', bg: '#fffbeb' },
  CERTIFICATION_EXPIRED: { Icon: ShieldAlert, color: '#e11d48', bg: '#fff1f2' },
  ASSESSMENT_REMINDER: { Icon: Bell, color: '#2563eb', bg: '#eff6ff' },
  TRAINING_DEADLINE_APPROACHING: { Icon: GraduationCap, color: '#f59e0b', bg: '#fffbeb' },
  TRAINING_DEADLINE_OVERDUE: { Icon: GraduationCap, color: '#e11d48', bg: '#fff1f2' },
  RECOMMENDATION_NEW: { Icon: Sparkles, color: '#0d9488', bg: '#f0fdfa' },
  MENTORSHIP_SESSION_REMINDER: { Icon: CalendarClock, color: '#2563eb', bg: '#eff6ff' },
  MILESTONE_ACHIEVED: { Icon: Trophy, color: '#9333ea', bg: '#faf5ff' },
  GENERAL: { Icon: Bell, color: '#64748b', bg: '#f1f5f9' },
};

export const TYPE_LABEL = {
  CRITICAL_GAP: 'Critical Skill Gap',
  MODERATE_GAP: 'Moderate Skill Gap',
  CERTIFICATION_EXPIRING: 'Certification Expiring',
  CERTIFICATION_EXPIRED: 'Certification Expired',
  ASSESSMENT_REMINDER: 'Assessment Reminder',
  TRAINING_DEADLINE_APPROACHING: 'Training Deadline',
  TRAINING_DEADLINE_OVERDUE: 'Training Overdue',
  RECOMMENDATION_NEW: 'New Recommendation',
  MENTORSHIP_SESSION_REMINDER: 'Mentorship Session',
  MILESTONE_ACHIEVED: 'Milestone Achieved',
  GENERAL: 'General',
};

// Used to build the filter tabs on the Notification Center page.
export const FILTER_GROUPS = [
  { key: 'ALL', label: 'All', types: null },
  { key: 'GAPS', label: 'Skill Gaps', types: ['CRITICAL_GAP', 'MODERATE_GAP'] },
  { key: 'CERTS', label: 'Certifications', types: ['CERTIFICATION_EXPIRING', 'CERTIFICATION_EXPIRED'] },
  { key: 'TRAINING', label: 'Training', types: ['TRAINING_DEADLINE_APPROACHING', 'TRAINING_DEADLINE_OVERDUE'] },
  { key: 'MENTORSHIP', label: 'Mentorship', types: ['MENTORSHIP_SESSION_REMINDER'] },
  { key: 'RECOMMENDATIONS', label: 'Recommendations', types: ['RECOMMENDATION_NEW'] },
  { key: 'MILESTONES', label: 'Milestones', types: ['MILESTONE_ACHIEVED'] },
];

export function timeAgo(dateStr) {
  const diffMs = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}
