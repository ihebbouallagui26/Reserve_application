import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {RestaurantMap} from '../RestaurantMap';
import type {MappableRestaurant} from '../RestaurantMap';

jest.mock('react-native-maps');

function render(restaurants: MappableRestaurant[], props: Partial<React.ComponentProps<typeof RestaurantMap>> = {}) {
  const onSelectRestaurant = jest.fn();
  const onOpenRestaurant = jest.fn();
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(
      <RestaurantMap
        restaurants={restaurants}
        selectedRestaurantId={null}
        onSelectRestaurant={onSelectRestaurant}
        onOpenRestaurant={onOpenRestaurant}
        {...props}
      />,
    );
  });
  return {tree, onSelectRestaurant, onOpenRestaurant};
}

function textOf(tree: ReactTestRenderer.ReactTestRenderer): string {
  return JSON.stringify(tree.toJSON());
}

describe('RestaurantMap', () => {
  it('shows an explanatory state instead of an empty map when no restaurant has coordinates', () => {
    // RestaurantPublicView never carries lat/lng today (Phase 7/8 contract) -
    // this is the real, honest state every caller sees right now.
    const {tree} = render([{id: 'r1', name: 'Le Rezkna'}, {id: 'r2', name: 'Chez Amina'}]);
    expect(textOf(tree)).toContain("La géolocalisation des restaurants n'est pas encore disponible.");
    expect(tree.root.findAllByProps({testID: 'restaurant-map'})).toHaveLength(0);
  });

  it('plots a marker only for restaurants that do have coordinates, never fabricating the rest', () => {
    const {tree} = render([
      {id: 'r1', name: 'Le Rezkna', latitude: 36.8065, longitude: 10.1815},
      {id: 'r2', name: 'Chez Amina'},
    ]);
    expect(tree.root.findAllByProps({testID: 'restaurant-map'}).length).toBeGreaterThan(0);
    expect(tree.root.findAllByProps({accessibilityLabel: 'Le Rezkna'}).length).toBeGreaterThan(0);
    expect(tree.root.findAllByProps({accessibilityLabel: 'Chez Amina'})).toHaveLength(0);
  });

  it('tapping a marker selects it (does not open the detail screen)', () => {
    const {tree, onSelectRestaurant, onOpenRestaurant} = render([
      {id: 'r1', name: 'Le Rezkna', latitude: 36.8065, longitude: 10.1815},
    ]);
    const marker = tree.root.findAllByProps({accessibilityLabel: 'Le Rezkna'})[0];
    act(() => {
      marker.props.onPress();
    });
    expect(onSelectRestaurant).toHaveBeenCalledWith('r1');
    expect(onOpenRestaurant).not.toHaveBeenCalled();
  });

  it('tapping a marker\'s callout opens the restaurant detail screen', () => {
    const {tree, onOpenRestaurant} = render([
      {id: 'r1', name: 'Le Rezkna', latitude: 36.8065, longitude: 10.1815},
    ]);
    const marker = tree.root.findAllByProps({accessibilityLabel: 'Le Rezkna'})[0];
    act(() => {
      marker.props.onCalloutPress();
    });
    expect(onOpenRestaurant).toHaveBeenCalledWith('r1');
  });
});
