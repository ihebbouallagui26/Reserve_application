import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {ExploreScreen} from '../ExploreScreen';
import {LoadingState} from '../../../../components/LoadingState';
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

// This suite exercises the phone composition specifically - force a phone-
// sized window rather than relying on the Jest RN preset's default (which
// classifies as tablet under useDeviceType's 600dp threshold).
jest.mock('react-native/Libraries/Utilities/useWindowDimensions', () => ({
  __esModule: true,
  default: () => ({width: 375, height: 812}),
}));

// Rendered on phone via CustomBottomNavigation, which needs a safe-area context.
jest.mock('react-native-safe-area-context', () => {
  const actual = jest.requireActual('react-native-safe-area-context');
  return {
    ...actual,
    useSafeAreaInsets: () => ({top: 0, bottom: 0, left: 0, right: 0}),
  };
});

const mockedList = restaurantApi.list as jest.Mock;
const mockedSearch = restaurantApi.search as jest.Mock;
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

describe('ExploreScreen', () => {
  beforeEach(() => {
    mockedList.mockReset();
    mockedSearch.mockReset();
    mockNavigate.mockReset();
    // Denied is the fallback path (restaurantApi.list()) that every test in
    // this describe block already assumes - location-specific behavior gets
    // its own describe block below.
    mockedGetCurrentPosition.mockReset().mockResolvedValue({status: 'denied'});
  });

  it('shows a loading state while fetching', () => {
    mockedList.mockReturnValue(new Promise(() => {}));
    const tree = renderScreen();
    expect(tree.root.findAllByType(LoadingState)).toHaveLength(1);
  });

  it('renders the restaurant list on success', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();
    const text = textOf(tree);
    expect(text).toContain('Le Rezkna');
    expect(text).toContain('Chez Amina');
  });

  it('shows the empty state when the backend returns no restaurants', async () => {
    mockedList.mockResolvedValue([]);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain("Nous n'avons trouvé aucun restaurant.");
  });

  it('shows the error state and retries on demand', async () => {
    mockedList
      .mockRejectedValueOnce({kind: 'network', status: null, message: 'Impossible de contacter le serveur.'})
      .mockResolvedValueOnce(sample);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Impossible de contacter le serveur.');
    expect(textOf(tree)).toContain('Réessayer');

    const retryButton = tree.root.findByProps({label: 'Réessayer'});
    act(() => {
      retryButton.props.onPress();
    });
    await flush();
    expect(mockedList).toHaveBeenCalledTimes(2);
    expect(textOf(tree)).toContain('Le Rezkna');
  });

  it('filters the loaded list locally, case-insensitively, without calling the API again', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();

    const searchInput = tree.root.findByProps({accessibilityLabel: 'Rechercher un restaurant'});
    act(() => {
      searchInput.props.onChangeText('AMINA');
    });

    const text = textOf(tree);
    expect(text).toContain('Chez Amina');
    expect(text).not.toContain('Le Rezkna');
    expect(mockedList).toHaveBeenCalledTimes(1);
  });

  it('navigates to RestaurantDetail with the tapped restaurant id', async () => {
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();

    const card = tree.root.findByProps({accessibilityLabel: 'Le Rezkna, Tunis'});
    act(() => {
      card.props.onPress();
    });

    expect(mockNavigate).toHaveBeenCalledWith('RestaurantDetail', {restaurantId: 'r1'});
  });
});

describe('ExploreScreen location behavior', () => {
  beforeEach(() => {
    mockedList.mockReset();
    mockedSearch.mockReset();
    mockedGetCurrentPosition.mockReset();
  });

  it('searches near the diner when permission is granted, with the documented default radius', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'success', latitude: 36.8065, longitude: 10.1815});
    mockedSearch.mockResolvedValue(sample);
    renderScreen();
    await flush();
    expect(mockedSearch).toHaveBeenCalledWith({lat: 36.8065, lng: 10.1815, radiusKm: 10});
    expect(mockedList).not.toHaveBeenCalled();
  });

  it('falls back to the unfiltered list when permission is denied', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'denied'});
    mockedList.mockResolvedValue(sample);
    renderScreen();
    await flush();
    expect(mockedList).toHaveBeenCalledTimes(1);
    expect(mockedSearch).not.toHaveBeenCalled();
  });

  it('falls back to the unfiltered list when the GPS is unavailable', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'unavailable'});
    mockedList.mockResolvedValue(sample);
    renderScreen();
    await flush();
    expect(mockedList).toHaveBeenCalledTimes(1);
  });

  it('falls back to the unfiltered list on a location timeout', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'timeout'});
    mockedList.mockResolvedValue(sample);
    renderScreen();
    await flush();
    expect(mockedList).toHaveBeenCalledTimes(1);
  });

  it('never becomes unusable when location fails - the fallback list still renders', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'unavailable'});
    mockedList.mockResolvedValue(sample);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Le Rezkna');
  });

  it('shows a distance-aware empty message when the geo search itself returns nothing', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'success', latitude: 36.8065, longitude: 10.1815});
    mockedSearch.mockResolvedValue([]);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Nous n\'avons trouvé aucun restaurant à proximité.');
  });

  it('calls getCurrentPosition exactly once per load - never in a loop', async () => {
    mockedGetCurrentPosition.mockResolvedValue({status: 'denied'});
    mockedList.mockResolvedValue(sample);
    renderScreen();
    await flush();
    expect(mockedGetCurrentPosition).toHaveBeenCalledTimes(1);
  });
});
