import React from 'react';
import ReactTestRenderer, {act} from 'react-test-renderer';
import {CustomBottomNavigation} from '../CustomBottomNavigation';

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({navigate: mockNavigate}),
}));

jest.mock('react-native-safe-area-context', () => ({
  useSafeAreaInsets: () => ({top: 0, bottom: 0, left: 0, right: 0}),
}));

function render(active: 'explore' | 'map' | 'account') {
  let tree!: ReactTestRenderer.ReactTestRenderer;
  act(() => {
    tree = ReactTestRenderer.create(<CustomBottomNavigation active={active} />);
  });
  return tree;
}

/** accessibilityLabel/accessibilityState/onPress cascade onto Pressable's own
 * internal host node too, so findByProps (which requires exactly one match)
 * throws - take the first of the (possibly several) matches instead. */
function findFirstByLabel(tree: ReactTestRenderer.ReactTestRenderer, label: string) {
  return tree.root.findAllByProps({accessibilityLabel: label})[0];
}

describe('CustomBottomNavigation', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
  });

  it('renders the three expected tabs', () => {
    const tree = render('explore');
    expect(tree.root.findAllByProps({accessibilityLabel: 'Explore'}).length).toBeGreaterThan(0);
    expect(tree.root.findAllByProps({accessibilityLabel: 'Map'}).length).toBeGreaterThan(0);
    expect(tree.root.findAllByProps({accessibilityLabel: 'Account'}).length).toBeGreaterThan(0);
  });

  it('marks the active tab as selected', () => {
    const tree = render('map');
    expect(findFirstByLabel(tree, 'Map').props.accessibilityState).toEqual({selected: true});
    expect(findFirstByLabel(tree, 'Explore').props.accessibilityState).toEqual({selected: false});
  });

  it('navigates to Explore/Map/Account when each tab is pressed', () => {
    const tree = render('explore');

    act(() => {
      findFirstByLabel(tree, 'Map').props.onPress();
    });
    expect(mockNavigate).toHaveBeenCalledWith('Map');

    act(() => {
      findFirstByLabel(tree, 'Account').props.onPress();
    });
    expect(mockNavigate).toHaveBeenCalledWith('Account');
  });
});
