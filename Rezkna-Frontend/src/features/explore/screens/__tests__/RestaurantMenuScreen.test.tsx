import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {RestaurantMenuScreen} from '../RestaurantMenuScreen';
import {menuApi} from '../../../../services/api/menuApi';

jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({canGoBack: () => true, goBack: jest.fn()}),
}));

jest.mock('../../../../services/api/menuApi', () => ({
  menuApi: {getByRestaurantId: jest.fn()},
}));

const mockedGetByRestaurantId = menuApi.getByRestaurantId as jest.Mock;

const items = [
  {id: 'i1', name: 'Couscous', description: 'Semoule, légumes', price: 12.5, category: 'Plats', available: true},
  {id: 'i2', name: 'Brik', description: null, price: 4, category: 'Entrées', available: true},
  {id: 'i3', name: 'Tajine épuisé', description: null, price: 15, category: 'Plats', available: false},
  {id: 'i4', name: 'Eau', description: null, price: 2, category: null, available: true},
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
  const route = {key: 'RestaurantMenu', name: 'RestaurantMenu' as const, params: {restaurantId: 'r1'}};
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(
      <RestaurantMenuScreen route={route as never} navigation={{} as never} />,
    );
  });
  return tree;
}

describe('RestaurantMenuScreen', () => {
  beforeEach(() => {
    mockedGetByRestaurantId.mockReset();
  });

  it('groups items by category and preserves multiple categories', async () => {
    mockedGetByRestaurantId.mockResolvedValue({items});
    const tree = renderScreen();
    await flush();
    expect(mockedGetByRestaurantId).toHaveBeenCalledWith('r1');
    const text = textOf(tree);
    expect(text).toContain('Plats');
    expect(text).toContain('Entrées');
    expect(text).toContain('Couscous');
    expect(text).toContain('Brik');
  });

  it('buckets items with a null/blank category under "Autres"', async () => {
    mockedGetByRestaurantId.mockResolvedValue({items});
    const tree = renderScreen();
    await flush();
    const text = textOf(tree);
    expect(text).toContain('Autres');
    expect(text).toContain('Eau');
  });

  it('shows an elegant empty state for a menu with zero items', async () => {
    mockedGetByRestaurantId.mockResolvedValue({items: []});
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Le menu de ce restaurant est actuellement vide.');
  });

  it('marks an unavailable item clearly without removing it', async () => {
    mockedGetByRestaurantId.mockResolvedValue({items});
    const tree = renderScreen();
    await flush();
    const text = textOf(tree);
    expect(text).toContain('Tajine épuisé');
    expect(text).toContain('Indisponible');
  });

  it('shows a not-found message for a 404', async () => {
    mockedGetByRestaurantId.mockRejectedValue({kind: 'notFound', status: 404, message: 'Introuvable'});
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Ce restaurant est introuvable.');
  });

  it('shows a retryable error state on a network error', async () => {
    mockedGetByRestaurantId.mockRejectedValue({kind: 'network', status: null, message: 'Impossible de contacter le serveur.'});
    const tree = renderScreen();
    await flush();
    expect(textOf(tree)).toContain('Impossible de contacter le serveur.');
    expect(textOf(tree)).toContain('Réessayer');
  });
});
