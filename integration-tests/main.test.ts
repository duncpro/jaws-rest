import fetch from 'node-fetch';
import * as fs from 'fs';

const restApiBaseUrl = process.env.API_URL;

interface EchoResponseBody {
    requestBody: string,
    requestMethod: string,
    requestQueryParams: { [key: string]: string },
    requestPath: string
}

const GENERAL_API_CALL_TIMEOUT = 1000 * 30;

describe('integration tests', () => {
    test('API endpoint responds with status code 200 and expected payload', async () => {
        const response = await fetch(restApiBaseUrl + '/pets/1234', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: 'Cocoa'
            })
        });

        expect(response.status).toEqual(200);
        expect(await response.json()).toEqual({
            petId: '1234',
            name: 'Cocoa'
        });
    }, GENERAL_API_CALL_TIMEOUT);
});
