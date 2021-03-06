import * as cdk from '@aws-cdk/core';
import {CfnOutput, Duration} from '@aws-cdk/core';
import {LambdaRestApi} from '@aws-cdk/aws-apigateway';
import {Code, Function, Runtime} from '@aws-cdk/aws-lambda'
import {
  AuroraCapacityUnit,
  AuroraPostgresEngineVersion,
  DatabaseClusterEngine,
  ServerlessCluster
} from '@aws-cdk/aws-rds';
import {Vpc} from '@aws-cdk/aws-ec2';

export class MainStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const masterVpc = new Vpc(this, 'MasterVPC', {});

    const masterDb = new ServerlessCluster(this, 'MasterDB', {
      enableDataApi: true,
      scaling: {
        autoPause: Duration.minutes(5),
        minCapacity: AuroraCapacityUnit.ACU_2,
        maxCapacity: AuroraCapacityUnit.ACU_2
      },
      vpc: masterVpc,
      engine: DatabaseClusterEngine.auroraPostgres({
        version: AuroraPostgresEngineVersion.VER_10_16
      }),
    });

    new CfnOutput(this, 'MasterDbArn', {
      value: masterDb.clusterArn
    });

    new CfnOutput(this, 'MasterDbSecretArn', {
      value: masterDb.secret?.secretArn!
    });

    const requestHandler = new Function(this, 'RestRequestHandler', {
      handler: 'com.duncpro.jaws.AWSLambdaEntryPoint',
      runtime: Runtime.JAVA_11,
      code: Code.fromAsset(process.env.PATH_TO_REQUEST_HANDLER_PACKAGE!),
      environment: {
        'MASTER_DB_ARN': masterDb.clusterArn,
        'MASTER_DB_SECRET_ARN': masterDb.secret?.secretArn!
      },
      memorySize: 2048,
      timeout: Duration.seconds(30) // Cold starts are slow
    });

    masterDb.grantDataApiAccess(requestHandler);

    const mainRestApi = new LambdaRestApi(this, 'MainRestApi', {
      handler: requestHandler,
    });

    new CfnOutput(this, 'MainRestApiUrl', {
      value: mainRestApi.deploymentStage.urlForPath('/')
    })
  }
}

const app = new cdk.App();
new MainStack(app, 'MainStack', {
  /* If you don't specify 'env', this stack will be environment-agnostic.
   * Account/Region-dependent features and context lookups will not work,
   * but a single synthesized template can be deployed anywhere. */

  /* Uncomment the next line to specialize this stack for the AWS Account
   * and Region that are implied by the current CLI configuration. */
  // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

  /* Uncomment the next line if you know exactly what Account and Region you
   * want to deploy the stack to. */
  // env: { account: '123456789012', region: 'us-east-1' },

  /* For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html */
});
