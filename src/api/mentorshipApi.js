import api from './axios';

export const mentorshipApi = {
  findExperts: (skillName) => api.get('/mentorship/experts', { params: skillName ? { skillName } : {} }),
  bookSession: (data) => api.post('/mentorship/sessions', data),
  getMySessions: () => api.get('/mentorship/sessions/me'),
  updateSessionStatus: (id, status) => api.patch(`/mentorship/sessions/${id}/status`, { status }),
};
