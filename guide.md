# Guide

## What does `dom/With` do?

`dom/With` runs an Electric DOM body inside an existing browser DOM element.

In this project:

```clojure
(e/$ dom/With
  (e/client (.getElementById js/document "app"))
  ...)
```

The browser already has `<div id="app"></div>` from `index.html`. `dom/With` tells Electric to mount the app inside that existing node instead of creating a fresh root element itself.

## What does `e/drain` do?

`e/drain` runs Electric code for its side effects and discards its value.

In this project:

```clojure
(e/client
  (e/drain
    (render-staff! dom/node chord)))
```

`render-staff!` mutates the DOM by asking VexFlow to redraw the stave. There is no useful Electric value to return from that expression, so `e/drain` is used to say “run this whenever dependencies change, but don’t treat its return value as part of the UI result”.

## How come the top level `App` has an `e/fn []`?

`dom/With` expects a body function, not raw body forms.

So this:

```clojure
(e/$ dom/With
  (e/client (.getElementById js/document "app"))
  (e/fn []
    ...))
```

means:

- first argument: the existing DOM element to mount into
- second argument: the Electric function to run inside that element

Without the `e/fn []`, there is no body function for `dom/With` to call.
