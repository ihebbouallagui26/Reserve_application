import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import {AccountEntryScreen} from '../AccountEntryScreen';
import {useSession} from '../../../../session/SessionProvider';
import {AuthNavigator} from '../../../auth/navigation/AuthNavigator';
import {DinerNavigator} from '../../../diner/navigation/DinerNavigator';

jest.mock('../../../../session/SessionProvider', () => ({
  useSession: jest.fn(),
}));

jest.mock('../../../auth/navigation/AuthNavigator', () => ({
  AuthNavigator: () => null,
}));

jest.mock('../../../diner/navigation/DinerNavigator', () => ({
  DinerNavigator: () => null,
}));

const mockedUseSession = useSession as jest.Mock;

function render(): ReactTestRenderer.ReactTestRenderer {
  let tree!: ReactTestRenderer.ReactTestRenderer;
  ReactTestRenderer.act(() => {
    tree = ReactTestRenderer.create(<AccountEntryScreen />);
  });
  return tree;
}

describe('AccountEntryScreen', () => {
  it('renders AuthNavigator for a guest', () => {
    mockedUseSession.mockReturnValue({status: 'none'});
    const tree = render();
    expect(tree.root.findAllByType(AuthNavigator)).toHaveLength(1);
    expect(tree.root.findAllByType(DinerNavigator)).toHaveLength(0);
  });

  it('renders DinerNavigator for a signed-in diner', () => {
    mockedUseSession.mockReturnValue({status: 'diner'});
    const tree = render();
    expect(tree.root.findAllByType(DinerNavigator)).toHaveLength(1);
    expect(tree.root.findAllByType(AuthNavigator)).toHaveLength(0);
  });
});
