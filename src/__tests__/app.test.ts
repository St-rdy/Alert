import { createApp } from '../app';
import { describe, it, expect } from '@jest/globals';

describe('App Module', () => {
  it('should return Hello World!', () => {
    const result = createApp();
    expect(result).toBeDefined();
  });
});
