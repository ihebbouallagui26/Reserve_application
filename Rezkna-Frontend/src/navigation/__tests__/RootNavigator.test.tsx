import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import {RootNavigator} from '../RootNavigator';
import {useSession} from '../../session/SessionProvider';
import {MainNavigator} from '../MainNavigator';
import {PartnerNavigator} from '../../features/partner/navigation/PartnerNavigator';

jest.mock('../../session/SessionProvider', () => ({
  useSession: jest.fn(),
}));

// Leaf navigators are mocked so this suite exercises routing only, not their
// own internals (ExploreScreen/PartnerNavigator's screens get their own
// dedicated tests elsewhere).
jest.mock('../MainNavigator', () => ({
  MainNavigator: () => null,
}));

jest.mock('../../features/partner/navigation/PartnerNavigator', () => ({
  PartnerNavigator: () => null,
}));

const mockedUseSession = useSession as jest.Mock;

function renderRoot(): ReactTestRenderer.ReactTestRenderer {
  let tree!: ReactTestRenderer.ReactTestRenderer;
  ReactTestRenderer.act(() => {
    tree = ReactTestRenderer.create(<RootNavigator />);
  });
  return tree;
}

describe('RootNavigator', () => {
  it('renders nothing while the session is loading', () => {
    mockedUseSession.mockReturnValue({status: 'loading'});
    expect(renderRoot().toJSON()).toBeNull();
  });

  it('mounts MainNavigator for a guest (status "none") - Explore must be public', () => {
    mockedUseSession.mockReturnValue({status: 'none'});
    const tree = renderRoot();
    expect(tree.root.findAllByType(MainNavigator)).toHaveLength(1);
    expect(tree.root.findAllByType(PartnerNavigator)).toHaveLength(0);
  });

  it('mounts MainNavigator for a signed-in diner', () => {
    mockedUseSession.mockReturnValue({status: 'diner'});
    const tree = renderRoot();
    expect(tree.root.findAllByType(MainNavigator)).toHaveLength(1);
  });

  it('mounts PartnerNavigator for a partner, never MainNavigator', () => {
    mockedUseSession.mockReturnValue({status: 'partner'});
    const tree = renderRoot();
    expect(tree.root.findAllByType(PartnerNavigator)).toHaveLength(1);
    expect(tree.root.findAllByType(MainNavigator)).toHaveLength(0);
  });
});
