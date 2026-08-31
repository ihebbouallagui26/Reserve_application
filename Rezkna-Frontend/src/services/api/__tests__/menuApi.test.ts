import {menuApi} from '../menuApi';
import {apiRequest} from '../client';

jest.mock('../client', () => ({
  apiRequest: jest.fn(),
}));

const mockedApiRequest = apiRequest as jest.Mock;

describe('menuApi', () => {
  beforeEach(() => {
    mockedApiRequest.mockReset();
  });

  it('getByRestaurantId() calls the public menu endpoint for that restaurant', async () => {
    mockedApiRequest.mockResolvedValue({items: []});
    await menuApi.getByRestaurantId('r1');
    expect(mockedApiRequest).toHaveBeenCalledWith('/api/restaurant/restaurants/public/r1/menu');
  });
});
