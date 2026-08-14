import api from './axios';

export const trainingEnrollmentApi = {
  enroll: (data) => api.post('/training/enrollments', data),
  getMyEnrollments: () => api.get('/training/enrollments/me'),
  updateProgress: (id, progressPercent) => api.patch(`/training/enrollments/${id}/progress`, { progressPercent }),
  cancelEnrollment: (id) => api.delete(`/training/enrollments/${id}`),
  getTeamProgress: () => api.get('/training/enrollments/team'),
};
