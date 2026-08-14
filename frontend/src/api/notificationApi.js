import api from './axios';

export const notificationApi = {
  getMyNotifications: () => api.get('/notifications/me'),
  markAsRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllAsRead: () => api.patch('/notifications/read-all'),
  deleteNotification: (id) => api.delete(`/notifications/${id}`),
};
