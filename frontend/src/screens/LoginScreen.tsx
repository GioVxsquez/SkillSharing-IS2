import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  Alert, ActivityIndicator, KeyboardAvoidingView, Platform,
  ScrollView, StatusBar,
} from 'react-native';
import { api } from '../api/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

export default function LoginScreen({ navigation }: any) {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading]   = useState(false);
  const [focusEmail, setFocusEmail]   = useState(false);
  const [focusPass, setFocusPass]     = useState(false);

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Faltan datos', 'Ingresa tu correo y contraseña para continuar.');
      return;
    }
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { email, password });
      if (response.data.ok && response.data.data?.token) {
        await AsyncStorage.setItem('userToken', response.data.data.token);
        navigation.replace('Home');
      } else {
        Alert.alert('Error', response.data.mensaje || 'No se pudo iniciar sesión.');
      }
    } catch (error: any) {
      const msg = error.response?.data?.mensaje || 'No se pudo conectar al servidor.';
      Alert.alert('Error', msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">

        {/* Bloque superior con el nombre */}
        <View style={styles.topBlock}>
          <View style={styles.logoBadge}>
            <Text style={styles.logoBadgeLetra}>S</Text>
          </View>
          <Text style={styles.appNombre}>SkillSharing</Text>
          <Text style={styles.appSlogan}>Aprende de los que saben</Text>
        </View>

        {/* Formulario */}
        <View style={styles.formCard}>
          <Text style={styles.formTitulo}>Bienvenido de vuelta</Text>
          <Text style={styles.formSubtitulo}>Inicia sesión para continuar</Text>

          <View style={styles.campo}>
            <Text style={styles.label}>Correo</Text>
            <TextInput
              style={[styles.input, focusEmail && styles.inputFocus]}
              placeholder="tu@correo.com"
              placeholderTextColor="#8898AA"
              value={email}
              onChangeText={setEmail}
              keyboardType="email-address"
              autoCapitalize="none"
              autoCorrect={false}
              onFocus={() => setFocusEmail(true)}
              onBlur={() => setFocusEmail(false)}
            />
          </View>

          <View style={styles.campo}>
            <Text style={styles.label}>Contraseña</Text>
            <TextInput
              style={[styles.input, focusPass && styles.inputFocus]}
              placeholder="••••••••"
              placeholderTextColor="#8898AA"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              onFocus={() => setFocusPass(true)}
              onBlur={() => setFocusPass(false)}
            />
          </View>

          <TouchableOpacity
            style={[styles.boton, loading && styles.botonDeshabilitado]}
            onPress={handleLogin}
            disabled={loading}
            activeOpacity={0.85}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.botonTexto}>Entrar</Text>}
          </TouchableOpacity>

          <View style={styles.pie}>
            <Text style={styles.pieTexto}>¿Aun no tienes cuenta? </Text>
            <TouchableOpacity onPress={() => navigation.navigate('Register')}>
              <Text style={styles.enlace}>Regístrate</Text>
            </TouchableOpacity>
          </View>
        </View>

      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F1C36' },
  scroll: { flexGrow: 1 },
  topBlock: {
    alignItems: 'center',
    paddingTop: 80,
    paddingBottom: 40,
  },
  logoBadge: {
    width: 72,
    height: 72,
    borderRadius: 22,
    backgroundColor: '#F97316',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
    shadowColor: '#F97316',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.5,
    shadowRadius: 16,
    elevation: 10,
  },
  logoBadgeLetra: { color: '#FFFFFF', fontSize: 34, fontWeight: '900' },
  appNombre: { color: '#FFFFFF', fontSize: 28, fontWeight: '800', letterSpacing: 1 },
  appSlogan: { color: '#8898AA', fontSize: 14, marginTop: 6 },
  formCard: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 32,
    borderTopRightRadius: 32,
    paddingHorizontal: 28,
    paddingTop: 36,
    paddingBottom: 40,
  },
  formTitulo: { fontSize: 24, fontWeight: '800', color: '#0F1C36', marginBottom: 4 },
  formSubtitulo: { fontSize: 14, color: '#8898AA', marginBottom: 28 },
  campo: { marginBottom: 18 },
  label: { fontSize: 12, fontWeight: '700', color: '#4A5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: {
    backgroundColor: '#F7F9FC',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 16,
    fontSize: 15,
    color: '#0F1C36',
  },
  inputFocus: { borderColor: '#F97316', backgroundColor: '#FFFAF7' },
  boton: {
    backgroundColor: '#F97316',
    paddingVertical: 16,
    borderRadius: 14,
    alignItems: 'center',
    marginTop: 8,
    shadowColor: '#F97316',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.35,
    shadowRadius: 10,
    elevation: 6,
  },
  botonDeshabilitado: { opacity: 0.7 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '800', letterSpacing: 0.5 },
  pie: { flexDirection: 'row', justifyContent: 'center', marginTop: 28 },
  pieTexto: { color: '#718096', fontSize: 14 },
  enlace: { color: '#F97316', fontSize: 14, fontWeight: '700' },
});
