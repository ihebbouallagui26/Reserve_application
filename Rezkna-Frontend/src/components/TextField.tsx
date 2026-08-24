import React from 'react';
import {StyleSheet, Text, TextInput, View} from 'react-native';
import type {KeyboardTypeOptions, TextInputProps} from 'react-native';
import {radius, spacing, typography, useThemeColors} from '../theme';

interface TextFieldProps {
  label: string;
  value: string;
  onChangeText: (value: string) => void;
  placeholder?: string;
  secureTextEntry?: boolean;
  keyboardType?: KeyboardTypeOptions;
  autoCapitalize?: TextInputProps['autoCapitalize'];
  error?: string;
  editable?: boolean;
  maxLength?: number;
}

/** Base themed text input: label, error state and disabled state built in. */
export function TextField({
  label,
  value,
  onChangeText,
  placeholder,
  secureTextEntry,
  keyboardType,
  autoCapitalize = 'sentences',
  error,
  editable = true,
  maxLength,
}: TextFieldProps) {
  const colors = useThemeColors();
  return (
    <View style={styles.container}>
      <Text style={[typography.caption, {color: colors.textSecondary, marginBottom: spacing.xs}]}>
        {label}
      </Text>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.textSecondary}
        secureTextEntry={secureTextEntry}
        keyboardType={keyboardType}
        autoCapitalize={autoCapitalize}
        editable={editable}
        maxLength={maxLength}
        style={[
          styles.input,
          {
            borderColor: error ? colors.error : colors.border,
            color: colors.textPrimary,
            backgroundColor: colors.surfaceAlt,
          },
          !editable && styles.disabled,
        ]}
      />
      {error ? (
        <Text style={[typography.caption, {color: colors.error, marginTop: spacing.xs}]}>{error}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: spacing.md,
  },
  input: {
    minHeight: 48,
    borderWidth: 1,
    borderRadius: radius.md,
    paddingHorizontal: spacing.md,
    fontSize: typography.body.fontSize,
  },
  disabled: {
    opacity: 0.6,
  },
});
