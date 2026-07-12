import api from './axios';

export const employeeApi = {
  getMyProfile: () => api.get('/employee-profile/me'),
  updateMyProfile: (data) => api.put('/employee-profile/me', data),

  getMySkills: () => api.get('/employee-profile/me/skills'),
  addSkill: (data) => api.post('/employee-profile/me/skills', data),
  removeSkill: (skillId) => api.delete(`/employee-profile/me/skills/${skillId}`),

  getAllSkills: () => api.get('/skills'),
  getAllCategories: () => api.get('/skills/categories'),

  getMyCertifications: () => api.get('/employee-profile/me/certifications'),
  addCertification: (data) => api.post('/employee-profile/me/certifications', data),
  deleteCertification: (id) => api.delete(`/employee-profile/me/certifications/${id}`),

  getMyWorkExperience: () => api.get('/employee-profile/me/experience'),
  addWorkExperience: (data) => api.post('/employee-profile/me/experience', data),
  deleteWorkExperience: (id) => api.delete(`/employee-profile/me/experience/${id}`),

  getAllDepartments: () => api.get('/departments'),
  getAllProfiles: () => api.get('/employee-profile/all'),
  getProfileByUserId: (userId) => api.get(`/employee-profile/user/${userId}`),
  updateProfileAsAdmin: (userId, data) => api.put(`/employee-profile/user/${userId}`, data),

  uploadProfilePhoto: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/employee-profile/me/photo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  uploadResume: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/employee-profile/me/resume', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
