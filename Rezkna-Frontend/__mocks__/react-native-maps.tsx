import React from 'react';
import {View} from 'react-native';

/**
 * Manual mock for the native map module - react-native-maps renders a real
 * native view that Jest cannot mount. All props (including onPress/
 * onCalloutPress/coordinate/title) pass straight through onto a plain View,
 * so tests can find a marker by its accessibilityLabel and call its
 * callbacks directly, exactly like every other Pressable in this test suite.
 */
export function MapView({children, testID}: {children?: React.ReactNode; testID?: string}) {
  return <View testID={testID}>{children}</View>;
}

export function Marker(props: Record<string, unknown>) {
  return <View {...props} />;
}

export default MapView;
