import axios from 'axios';

export const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const rawAxios = axios.create({
  headers: { 'Content-Type': 'application/json' },
});
