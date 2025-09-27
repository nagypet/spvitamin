export class ConfigurableService<T>
{
  private _config: T | null = null;


  public configure(cfg: T): void
  {
    this._config = {...cfg};
  }


  public get config(): T
  {
    if (!this._config)
    {
      throw new Error('ConfigurableService is not configured. Call ConfigService.configure(...) during app initialization.');
    }
    return this._config;
  }


  // Helper to check if configured without throwing
  public get isConfigured(): boolean
  {
    return !!this._config;
  }
}
