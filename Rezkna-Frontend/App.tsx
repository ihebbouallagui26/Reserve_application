/**
 * REZKNA - Sprint 0 mobile connectivity check.
 *
 * Technical verification screen only: proves Mobile -> Gateway -> backend
 * works end-to-end. No real application UI belongs here yet - that starts
 * in Sprint 1.
 *
 * @format
 */

import React, {useState} from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
} from 'react-native';
import {apiRequest} from './src/services/api';

type ConnectionStatus =
  | {kind: 'idle'}
  | {kind: 'loading'}
  | {kind: 'success'; message: string}
  | {kind: 'error'; message: string};

function App() {
  const isDarkMode = useColorScheme() === 'dark';
  const [status, setStatus] = useState<ConnectionStatus>({kind: 'idle'});

  const testConnection = async () => {
    setStatus({kind: 'loading'});
    try {
      // Public demo endpoint (identity-service /ping via the Gateway) -
      // deliberately not /ping/secure, since the app has no JWT to send yet.
      const response = await apiRequest<string>('/api/identity/ping');
      setStatus(
        response.ok
          ? {kind: 'success', message: response.message}
          : {kind: 'error', message: response.message},
      );
    } catch {
      setStatus({kind: 'error', message: 'Unable to connect to backend'});
    }
  };

  return (
    <SafeAreaView
      style={[styles.container, isDarkMode && styles.containerDark]}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <View style={styles.content}>
        <Text style={[styles.title, isDarkMode && styles.textDark]}>
          REZKNA
        </Text>
        <Text style={[styles.subtitle, isDarkMode && styles.textDark]}>
          Backend connection
        </Text>

        <TouchableOpacity style={styles.button} onPress={testConnection}>
          <Text style={styles.buttonText}>Test Connection</Text>
        </TouchableOpacity>

        <Text style={[styles.statusLabel, isDarkMode && styles.textDark]}>
          Status:
        </Text>
        <Text style={[styles.statusValue, isDarkMode && styles.textDark]}>
          {status.kind === 'idle' && 'Not tested'}
          {status.kind === 'loading' && 'Testing...'}
          {status.kind === 'success' &&
            `Backend connected (${status.message})`}
          {status.kind === 'error' && `Error: ${status.message}`}
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#ffffff',
  },
  containerDark: {
    backgroundColor: '#121212',
  },
  content: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    color: '#666666',
    marginBottom: 24,
  },
  textDark: {
    color: '#ffffff',
  },
  button: {
    backgroundColor: '#1a73e8',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
    marginBottom: 24,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  statusLabel: {
    fontSize: 14,
    color: '#666666',
  },
  statusValue: {
    fontSize: 18,
    fontWeight: '600',
    marginTop: 4,
    textAlign: 'center',
  },
});

export default App;
