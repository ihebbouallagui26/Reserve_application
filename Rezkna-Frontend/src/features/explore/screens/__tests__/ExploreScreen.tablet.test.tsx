import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {ExploreScreen} from '../ExploreScreen';
import {restaurantApi} from '../../../../services/api/restaurantApi';
import {getCurrentPosition} from '../../../../services/locationService';

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({navigate: mockNavigate}),
}));

jest.mock('../../../../services/api/restaurantApi', () => ({
  restaurantApi: {list: jest.fn(), search: jest.fn()},
}));

jest.mock('../../../../services/locationService', () => ({
  getCurrentPosition: jest.fn(),
}));

jest.mock('react-native-maps');

let mockDimensions = {width: 1024, height: 768};
jest.mock('react-native/Libraries/Utilities/useWindowDimensions', () => ({
  __esModule: true,
  default: () => mockDimensions,
}));

const mockedList = restaurantApi.list as jest.Mock;
const mockedGetCurrentPosition = getCurrentPosition as jest.Mock;

const sample = [
  {id: 'r1', name: 'Le Rezkna', address: '1 Avenue Habib Bourguiba', city: 'Tunis'},
  {id: 'r2', name: 'Chez Amina', address: '5 Rue de Marseille', city: 'Sfax'},
];

async function flush() {
  await act(async () => {
    await Promise.resolve();
    await Promise.resolve();
  });
}

function textOf(tree: ReactTestRenderer.ReactTestRenderer): string {
  return JSON.stringify(tree.toJSON());
}

function renderScreen(): ReactTestRenderer.ReactTestRenderer {
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(<ExploreScreen />);
  });
  return tree;
}

describe('ExploreScreen on tablet (1024x768, landscape)', () => {
  beforeEach(() => {
    mockedList.mockReset();
    mockNavigate.mockReset();
    // Denied is the fallback path (restaurantApi.list()) every test in this
    // file already assumes - dedicated geolocation coverage lives in
    // ExploreScreen.test.tsx's own "location behavior" describe block.
    mockedGetCurrentPosition.mockReset().mockResolvedValue({status: 'denied'});
    mockDimensions = {width: 1024, height: 768};
  });

  it('renders the persistent header with REZKNA and an Account entry, no bottom bar', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('REZKNA');
    expect(tree.root.findAllByProps({accessibilityLabel: 'Account'}).length).toBeGreaterThan(0);
    // No bottom-nav tabs (Map/Explore tab labels only exist inside CustomBottomNavigation)
    expect(tree.root.findAllByProps({accessibilityRole: 'tab'})).toHaveLength(0);
  });

  it('a first tap on a card selects it without navigating away', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();

    const card = tree.root.findAllByProps({accessibilityLabel: 'Le Rezkna, Tunis'})[0];
    act(() => {
      card.props.onPress();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('a second tap on an already-selected card opens the detail screen', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();

    const card = () => tree.root.findAllByProps({accessibilityLabel: 'Le Rezkna, Tunis'})[0];
    act(() => {
      card().props.onPress();
    });
    act(() => {
      card().props.onPress();
    });

    expect(mockNavigate).toHaveBeenCalledWith('RestaurantDetail', {restaurantId: 'r1'});
  });

  it('tapping the header Account button navigates to Account', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();

    const accountButton = tree.root.findAllByProps({accessibilityLabel: 'Account'})[0];
    act(() => {
      accountButton.props.onPress();
    });

    expect(mockNavigate).toHaveBeenCalledWith('Account');
  });

  it('shows the map\'s honest no-coordinates state, since RestaurantPublicView carries none', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain("La géolocalisation des restaurants n'est pas encore disponible.");
  });
});
