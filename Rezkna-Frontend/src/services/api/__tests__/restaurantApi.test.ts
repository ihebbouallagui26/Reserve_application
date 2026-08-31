import {restaurantApi} from '../restaurantApi';
import {apiRequest} from '../client';

jest.mock('../client', () => ({
  apiRequest: jest.fn(),
}));

const mockedApiRequest = apiRequest as jest.Mock;

describe('restaurantApi', () => {
  beforeEach(() => {
    mockedApiRequest.mockReset();
  });

  it('list() calls the public restaurants endpoint with no options', async () => {
    mockedApiRequest.mockResolvedValue([]);
    await restaurantApi.list();
    expect(mockedApiRequest).toHaveBeenCalledWith('/api/restaurant/restaurants/public');
  });

  it('getById() calls the public restaurant detail endpoint', async () => {
    mockedApiRequest.mockResolvedValue({id: 'r1', name: 'Le Rezkna', address: 'A', city: 'Tunis'});
    await restaurantApi.getById('r1');
    expect(mockedApiRequest).toHaveBeenCalledWith('/api/restaurant/restaurants/public/r1');
  });

  it('search() calls the geo search endpoint with lat/lng/radiusKm', async () => {
    mockedApiRequest.mockResolvedValue([]);
    await restaurantApi.search({lat: 36.8065, lng: 10.1815, radiusKm: 5});
    expect(mockedApiRequest).toHaveBeenCalledWith(
      '/api/restaurant/restaurants/public/search?lat=36.8065&lng=10.1815&radiusKm=5',
    );
  });
});
