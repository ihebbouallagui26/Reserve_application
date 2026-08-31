import {identityApi} from '../identityApi';
import {apiRequest} from '../client';

jest.mock('../client', () => ({
  apiRequest: jest.fn(),
}));

const mockedApiRequest = apiRequest as jest.Mock;

describe('identityApi.getPublicConfig', () => {
  beforeEach(() => {
    mockedApiRequest.mockReset();
  });

  it('calls the public config endpoint without a token', async () => {
    mockedApiRequest.mockResolvedValue({googleClientId: '', facebookAppId: ''});
    await identityApi.getPublicConfig();
    expect(mockedApiRequest).toHaveBeenCalledWith('/api/identity/config');
  });
});
