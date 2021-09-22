import fetch from 'node-fetch';
import * as fs from 'fs';

const restApiBaseUrl = (() => {
    const cfnOutputsPath = process.env.PATH_TO_CFN_OUTPUTS!;
    const cfnOutputsContent = fs.readFileSync(cfnOutputsPath);
    const cfnOutputs = JSON.parse(cfnOutputsContent.toString());
    return cfnOutputs.MainStack.MainRestApiUrl
})();

interface EchoResponseBody {
    requestBody: string,
    requestMethod: string,
    requestQueryParams: { [key: string]: string },
    requestPath: string
}

const GENERAL_API_CALL_TIMEOUT = 1000 * 30;

describe('integration tests', () => {
    test('API endpoint responds with status code 200', async () => {
        const response = await fetch(restApiBaseUrl + '/pets/1234', {
            headers: {
                'Accept': 'application/json'
            }
        });

        expect(response.status).toEqual(200);
        expect(await response.json()).toEqual({
            petId: '1234'
        });
    }, GENERAL_API_CALL_TIMEOUT);
});
