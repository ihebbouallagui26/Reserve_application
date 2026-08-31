import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {MapScreen} from '../MapScreen';
import {restaurantApi} from '../../../../services/api/restaurantApi';

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({navigate: mockNavigate}),
}));

jest.mock('react-native-safe-area-context', () => {
  const actual = jest.requireActual('react-native-safe-area-context');
  return {
    ...actual,
    useSafeAreaInsets: () => ({top: 0, bottom: 0, left: 0, right: 0}),
  };
});

jest.mock('../../../../services/api/restaurantApi', () => ({
  restaurantApi: {list: jest.fn()},
}));

jest.mock('react-native-maps');

let mockDimensions = {width: 375, height: 812};
jest.mock('react-native/Libraries/Utilities/useWindowDimensions', () => ({
  __esModule: true,
  default: () => mockDimensions,
}));

const mockedList = restaurantApi.list as jest.Mock;

const sample = [{id: 'r1', name: 'Le Rezkna', address: '1 Avenue Habib Bourguiba', city: 'Tunis'}];

async function flush() {
  await act(async () => {
    await Promise.resolve();
    await Promise.resolve();
  });
}

function textOf(tree: ReactTestRenderer.ReactTestRenderer): string {
  return JSON.stringify(tree.toJSON());
}

function render(): ReactTestRenderer.ReactTestRenderer {
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(<MapScreen />);
  });
  return tree;
}

describe('MapScreen on phone (375x812)', () => {
  beforeEach(() => {
    mockDimensions = {width: 375, height: 812};
    mockedList.mockReset();
    mockNavigate.mockReset();
  });

  it('fetches its own restaurant list and shows the map once loaded', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = render();
    await flush();
    expect(mockedList).toHaveBeenCalledTimes(1);
    // No coordinates on RestaurantPublicView -> the map's honest empty state.
    expect(textOf(tree)).toContain("La géolocalisation des restaurants n'est pas encore disponible.");
  });

  it('shows a retryable error state on a network failure', async () => {
    mockedList.mockRejectedValue({kind: 'network', status: null, message: 'Impossible de contacter le serveur.'});
    const tree = render();
    await flush();
    expect(textOf(tree)).toContain('Impossible de contacter le serveur.');
    expect(textOf(tree)).toContain('Réessayer');
  });

  it('shows the custom bottom bar with Map active', () => {
    mockedList.mockReturnValue(new Promise(() => {}));
    const tree = render();
    const mapTabs = tree.root.findAllByProps({accessibilityLabel: 'Map'});
    expect(mapTabs.length).toBeGreaterThan(0);
    expect(mapTabs[0].props.accessibilityState).toEqual({selected: true});
  });
});

describe('MapScreen on tablet (1024x768)', () => {
  beforeEach(() => {
    mockDimensions = {width: 1024, height: 768};
    mockedList.mockReturnValue(new Promise(() => {}));
  });

  it('does not render the phone bottom bar', () => {
    const tree = render();
    expect(tree.root.findAllByProps({accessibilityRole: 'tab'})).toHaveLength(0);
  });
});
