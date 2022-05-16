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
* [`getPosition()`](#getposition)
* [`seek(...)`](#seek)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [`removeAllListeners()`](#removealllisteners)
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
initialize(options: { webLicenseKey?: string; androidLicenseKey?: string; iosLicenseKey?: string; googleCastId?: string; debug?: boolean; }) => any
```

| Param         | Type                                                                                                                                 |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ webLicenseKey?: string; androidLicenseKey?: string; iosLicenseKey?: string; googleCastId?: string; debug?: boolean; }</code> |

**Returns:** <code>any</code>

--------------------


### create(...)

```typescript
create(options: { webConfiguration?: { container: string; properties?: any; }; nativeConfiguration?: any; captions?: JWPlayerMediaCaption[]; tracks?: JWPlayerMediaTrack[]; }) => any
```

| Param         | Type                                                                                                                                 |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| **`options`** | <code>{ webConfiguration?: { container: string; properties?: any; }; nativeConfiguration?: any; captions?: {}; tracks?: {}; }</code> |

**Returns:** <code>any</code>

--------------------


### remove()

```typescript
remove() => any
```

**Returns:** <code>any</code>

--------------------


### getPosition()

```typescript
getPosition() => any
```

**Returns:** <code>any</code>

--------------------


### seek(...)

```typescript
seek(options: { position: number; }) => any
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ position: number; }</code> |

**Returns:** <code>any</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'playerEvent', listenerFunc: EventChangeListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

Listen for events in player

| Param              | Type                                           |
| ------------------ | ---------------------------------------------- |
| **`eventName`**    | <code>"playerEvent"</code>                     |
| **`listenerFunc`** | <code>(event: JWPlayerEvent) =&gt; void</code> |

**Returns:** <code>any</code>

**Since:** 1.0.0

--------------------


### addListener(...)

```typescript
addListener(eventName: 'fullScreenPlayerEvent', listenerFunc: EventChangeListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                           |
| ------------------ | ---------------------------------------------- |
| **`eventName`**    | <code>"fullScreenPlayerEvent"</code>           |
| **`listenerFunc`** | <code>(event: JWPlayerEvent) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'readyPlayerEvent', listenerFunc: EventChangeListener) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                           |
| ------------------ | ---------------------------------------------- |
| **`eventName`**    | <code>"readyPlayerEvent"</code>                |
| **`listenerFunc`** | <code>(event: JWPlayerEvent) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => any
```

Remove all listeners (including the network status changes) for this plugin.

**Returns:** <code>any</code>

**Since:** 1.0.0

--------------------


### Interfaces


#### JWPlayerMediaCaption

| Prop        | Type                |
| ----------- | ------------------- |
| **`file`**  | <code>string</code> |
| **`label`** | <code>string</code> |


#### JWPlayerMediaTrack

| Prop          | Type                 |
| ------------- | -------------------- |
| **`file`**    | <code>string</code>  |
| **`label`**   | <code>string</code>  |
| **`default`** | <code>boolean</code> |


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |

</docgen-api>
