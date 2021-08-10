export interface JWPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
