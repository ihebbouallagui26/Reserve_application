import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import {useDeviceType, useOrientation, useResponsiveLayout} from '../responsive';

let mockDimensions = {width: 375, height: 812};

// Mocking only this leaf module - not the whole `react-native` barrel - avoids
// eagerly re-evaluating native-module-backed exports (e.g. DevMenu) that only
// work through the RN Jest preset's own transform, not a raw requireActual spread.
jest.mock('react-native/Libraries/Utilities/useWindowDimensions', () => ({
  __esModule: true,
  default: () => mockDimensions,
}));

function renderHook<T>(hook: () => T): T {
  let result: T;
  function TestComponent() {
    result = hook();
    return null;
  }
  ReactTestRenderer.act(() => {
    ReactTestRenderer.create(React.createElement(TestComponent));
  });
  return result!;
}

describe('useDeviceType', () => {
  it('classifies a standard phone (375x812) as phone', () => {
    mockDimensions = {width: 375, height: 812};
    expect(renderHook(useDeviceType)).toBe('phone');
  });

  it('classifies an iPad-sized screen (768x1024) as tablet', () => {
    mockDimensions = {width: 768, height: 1024};
    expect(renderHook(useDeviceType)).toBe('tablet');
  });

  it('classifies a tablet in landscape (1024x768) as tablet via the shortest side', () => {
    mockDimensions = {width: 1024, height: 768};
    expect(renderHook(useDeviceType)).toBe('tablet');
  });

  it('classifies a large phone in landscape (812x375) as phone via the shortest side', () => {
    mockDimensions = {width: 812, height: 375};
    expect(renderHook(useDeviceType)).toBe('phone');
  });

  it('treats exactly 600dp as the tablet threshold (inclusive)', () => {
    mockDimensions = {width: 600, height: 900};
    expect(renderHook(useDeviceType)).toBe('tablet');
  });
});

describe('useOrientation', () => {
  it('reports portrait when height exceeds width', () => {
    mockDimensions = {width: 768, height: 1024};
    expect(renderHook(useOrientation)).toBe('portrait');
  });

  it('reports landscape when width exceeds height', () => {
    mockDimensions = {width: 1024, height: 768};
    expect(renderHook(useOrientation)).toBe('landscape');
  });
});

describe('useResponsiveLayout', () => {
  it('combines device type, orientation and raw dimensions', () => {
    mockDimensions = {width: 1024, height: 768};
    const layout = renderHook(useResponsiveLayout);
    expect(layout).toEqual({
      deviceType: 'tablet',
      orientation: 'landscape',
      width: 1024,
      height: 768,
    });
  });
});
