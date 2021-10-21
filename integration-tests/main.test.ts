import fetch from 'node-fetch';
import {AddPetRequestBody, LookupOwnerResponseBody} from 'dto-interfaces'
const restApiBaseUrl = process.env.API_URL;
const GENERAL_API_CALL_TIMEOUT = 1000 * 30;

test('POST pets', async () => {
    // Required Headers
    const headers = { 'Content-Type': 'application/json' }

    // Required Body
    const body: AddPetRequestBody = {
        petName: 'Cocoa',
        owner: 'Duncan'
    }

    const response = await fetch(restApiBaseUrl + '/pets',
        { method: 'POST', headers, body: JSON.stringify(body) });

    // Expected Response
    expect(response.status).toEqual(200);
}, GENERAL_API_CALL_TIMEOUT);

test('GET pets/*/owner', async () => {
    // Required Headers
    const headers = { 'Accept': 'application/json' }

    const response = await fetch(restApiBaseUrl + '/pets/Cocoa/owner', { method: 'GET', headers });

    // Expected Response
    expect(response.status).toEqual(200);

    // Expected Response Body
    const expectedResponseBody: LookupOwnerResponseBody = {
        didFindOwner: true,
        owner: 'Duncan'
    };
    expect(await response.json()).toEqual(expectedResponseBody);
}, GENERAL_API_CALL_TIMEOUT);
