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
        const response = await fetch(restApiBaseUrl);

        expect(response.status).toEqual(200);
    }, GENERAL_API_CALL_TIMEOUT);

    describe('proxy function access', () => {
        let echoResponse: EchoResponseBody;

        beforeAll(async () => {
            const response = await fetch(restApiBaseUrl + '/hello' + '/world?id=123', {
                method: 'POST',
                body: 'hello world'
            });
            const responseBodyContent = (await response.buffer()).toString();
            echoResponse = JSON.parse(responseBodyContent);
        }, GENERAL_API_CALL_TIMEOUT);

        test('Proxy function has access to HTTP method', async () => {
            expect(echoResponse.requestMethod).toEqual('POST');
        });

        test('Proxy function has access to request body', async () => {
            expect(echoResponse.requestBody).toEqual('hello world');
        });

        test('Proxy function has access to request path', async () => {
            // Notice that AWS includes a leading slash
            expect(echoResponse.requestPath).toEqual('/hello/world');
        });

        test('Proxy function has access to request query params', async () => {
            expect(echoResponse.requestQueryParams.id).toEqual('123');
        })
    });
});
