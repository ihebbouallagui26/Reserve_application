import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {RestaurantDetailScreen} from '../RestaurantDetailScreen';
import {restaurantApi} from '../../../../services/api/restaurantApi';

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({navigate: mockNavigate, canGoBack: () => true, goBack: jest.fn()}),
}));

jest.mock('../../../../services/api/restaurantApi', () => ({
  restaurantApi: {getById: jest.fn()},
}));

const mockedGetById = restaurantApi.getById as jest.Mock;

const restaurant = {id: 'r1', name: 'Le Rezkna', address: '1 Avenue Habib Bourguiba', city: 'Tunis'};

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
  const route = {key: 'RestaurantDetail', name: 'RestaurantDetail' as const, params: {restaurantId: 'r1'}};
  const navigation = {navigate: mockNavigate} as never;
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(
      <RestaurantDetailScreen route={route as never} navigation={navigation} />,
    );
  });
  return tree;
}

describe('RestaurantDetailScreen', () => {
  beforeEach(() => {
    mockedGetById.mockReset();
    mockNavigate.mockReset();
  });

  it('fetches by the id from route params and renders the restaurant on success', async () => {
    mockedGetById.mockResolvedValue(restaurant);
    const tree = renderScreen();
    await flush();
    expect(mockedGetById).toHaveBeenCalledWith('r1');
    const text = textOf(tree);
    expect(text).toContain('Le Rezkna');
    expect(text).toContain('Tunis');
    expect(text).toContain('1 Avenue Habib Bourguiba');
  });

  it('shows a not-found message for a 404', async () => {
    mockedGetById.mockRejectedValue({kind: 'notFound', status: 404, message: 'Cette ressource est introuvable.'});
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Ce restaurant est introuvable.');
  });

  it('shows a retryable error state on a network error', async () => {
    mockedGetById.mockRejectedValueOnce({kind: 'network', status: null, message: 'Impossible de contacter le serveur.'});
    mockedGetById.mockResolvedValueOnce(restaurant);
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Impossible de contacter le serveur.');

    const retryButton = tree.root.findByProps({label: 'Réessayer'});
    act(() => {
      retryButton.props.onPress();
    });
    await flush();
    expect(mockedGetById).toHaveBeenCalledTimes(2);
    expect(textOf(tree)).toContain('Le Rezkna');
  });

  it('navigates to RestaurantMenu with the same restaurantId', async () => {
    mockedGetById.mockResolvedValue(restaurant);
    const tree = renderScreen();
    await flush();

    const menuButton = tree.root.findByProps({label: 'Voir le menu'});
    act(() => {
      menuButton.props.onPress();
    });

    expect(mockNavigate).toHaveBeenCalledWith('RestaurantMenu', {restaurantId: 'r1'});
  });
});
