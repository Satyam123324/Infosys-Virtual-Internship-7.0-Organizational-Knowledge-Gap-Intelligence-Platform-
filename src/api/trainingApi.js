import api from './axios';

export const trainingApi = {
  getMyEnrollments: () => api.get('/training/enrollments/me'),
  enroll: (data) => api.post('/training/enrollments', data),
  updateProgress: (id, progressPercent) => api.patch(`/training/enrollments/${id}/progress`, { progressPercent }),
  cancelEnrollment: (id) => api.delete(`/training/enrollments/${id}`),
  getTeamProgress: () => api.get('/training/enrollments/team'),
  getMyMilestones: () => api.get('/training/enrollments/milestones/me'),
};
