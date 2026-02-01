import express, { Application } from 'express';

export const createApp = (): Application => {
  const app = express();

  app.get('/', (_req, res) => {
    res.send('Hello World!');
  });
  return app;
};
