export class ApiError extends Error {
  constructor(message: string, public readonly statusCode?: number) {
    super(message);
    this.name = 'ApiError';
  }
}

export class NotFoundError extends ApiError {
  constructor(resource: string, id: string) {
    super(`${resource} not found: ${id}`, 404);
    this.name = 'NotFoundError';
  }
}

export class ResponseParseError extends ApiError {
  constructor(message: string) {
    super(message);
    this.name = 'ResponseParseError';
  }
}
