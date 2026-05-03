import { describe, expect, it } from 'vitest';
import { api, rawAxios } from './api';

describe('axios clients', () => {
  it('api uses /api baseURL and JSON content-type', () => {
    expect(api.defaults.baseURL).toBe('/api');
    expect(api.defaults.headers['Content-Type']).toBe('application/json');
  });

  it('rawAxios has no baseURL but JSON content-type', () => {
    expect(rawAxios.defaults.baseURL).toBeUndefined();
    expect(rawAxios.defaults.headers['Content-Type']).toBe('application/json');
  });
});
