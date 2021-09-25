import fetch from 'node-fetch';

const restApiBaseUrl = process.env.API_URL;

const GENERAL_API_CALL_TIMEOUT = 1000 * 30;

describe('integration tests', () => {
    test('POST pets', async () => {
        const response = await fetch(restApiBaseUrl + '/pets', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                petName: 'Cocoa',
                owner: 'Duncan'
            })
        });

        expect(response.status).toEqual(200);
    }, GENERAL_API_CALL_TIMEOUT);

    test('GET pets/*/owner', async () => {
        const response = await fetch(restApiBaseUrl + '/pets/Cocoa/owner', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
            },
        });

        expect(response.status).toEqual(200);
        expect(await response.json()).toEqual({
            didFindOwner: true,
            owner: 'Duncan'
        });
    }, GENERAL_API_CALL_TIMEOUT);
});
