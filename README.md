# jwplayer-capacitor

Integration of JWPlayer Web and Mobile SDKs with Capacitor

## Install

```bash
npm install jwplayer-capacitor
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`initialize(...)`](#initialize)
* [`create(...)`](#create)
* [`remove()`](#remove)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => any
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>any</code>

--------------------


### initialize(...)

```typescript
initialize(options: { webLicenseKey?: string; androidLicenseKey?: string; iosLicenseKey?: string; googleCastId?: string; }) => any
```

| Param         | Type                                                                                                                |
| ------------- | ------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ webLicenseKey?: string; androidLicenseKey?: string; iosLicenseKey?: string; googleCastId?: string; }</code> |

**Returns:** <code>any</code>

--------------------


### create(...)

```typescript
create(options: { divId?: string; videoURL: string; posterURL?: string; forceFullScreenOnLandscape?: boolean; x: number; y: number; width: number; height: number; captions?: Array<JWPlayerMediaTrack>; front?: boolean; }) => any
```

| Param         | Type                                                                                                                                                                                               |
| ------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ divId?: string; videoURL: string; posterURL?: string; forceFullScreenOnLandscape?: boolean; x: number; y: number; width: number; height: number; captions?: any; front?: boolean; }</code> |

**Returns:** <code>any</code>

--------------------


### remove()

```typescript
remove() => any
```

**Returns:** <code>any</code>

--------------------


### Interfaces


#### JWPlayerMediaTrack

| Prop          | Type                 |
| ------------- | -------------------- |
| **`url`**     | <code>string</code>  |
| **`label`**   | <code>string</code>  |
| **`default`** | <code>boolean</code> |

</docgen-api>
