import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  Alert, ActivityIndicator, KeyboardAvoidingView, Platform,
  ScrollView, Image, StatusBar,
} from 'react-native';
import { api } from '../api/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

export default function LoginScreen({ navigation }: any) {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading]   = useState(false);

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Campos incompletos', 'Por favor ingresa tu email y contraseña.');
      return;
    }
    setLoading(true);
    try {
      const response = await api.post('/auth/login', { email, password });
      if (response.data.exito && response.data.data?.token) {
        await AsyncStorage.setItem('userToken', response.data.data.token);
        navigation.replace('Home');
      } else {
        Alert.alert('Error', response.data.mensaje || 'Error al iniciar sesión.');
      }
    } catch (error: any) {
      const msg = error.response?.data?.mensaje || 'Error al conectar con el servidor.';
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
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">

        {/* Logo */}
        <View style={styles.logoContainer}>
          <Image source={require('../../assets/logo.png')} style={styles.logo} resizeMode="contain" />
        </View>

        <Text style={styles.titulo}>Inicia Sesión</Text>

        {/* Campos */}
        <View style={styles.campo}>
          <Text style={styles.label}>Correo Electrónico</Text>
          <TextInput
            style={styles.input}
            placeholder="correo@ejemplo.com"
            placeholderTextColor="#B0B8C1"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            autoCorrect={false}
          />
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Contraseña</Text>
          <TextInput
            style={styles.input}
            placeholder="••••••••"
            placeholderTextColor="#B0B8C1"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
        </View>

        {/* Botón principal */}
        <TouchableOpacity
          style={[styles.botonPrincipal, loading && styles.botonDeshabilitado]}
          onPress={handleLogin}
          disabled={loading}
          activeOpacity={0.85}
        >
          {loading
            ? <ActivityIndicator color="#fff" />
            : <Text style={styles.botonTexto}>Iniciar Sesión</Text>}
        </TouchableOpacity>

        {/* Enlace a Registro */}
        <View style={styles.pie}>
          <Text style={styles.pieTexto}>¿No tienes cuenta? </Text>
          <TouchableOpacity onPress={() => navigation.navigate('Register')}>
            <Text style={styles.enlace}>Regístrate</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scroll: {
    flexGrow: 1,
    justifyContent: 'center',
    paddingHorizontal: 32,
    paddingVertical: 40,
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 36,
  },
  logo: {
    width: 200,
    height: 70,
  },
  titulo: {
    fontSize: 26,
    fontWeight: '700',
    color: '#1B3A6B',
    marginBottom: 28,
    textAlign: 'center',
  },
  campo: {
    marginBottom: 20,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#4A5568',
    marginBottom: 8,
    letterSpacing: 0.3,
  },
  input: {
    backgroundColor: '#F7F9FC',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 10,
    paddingVertical: 14,
    paddingHorizontal: 16,
    fontSize: 15,
    color: '#1A202C',
  },
  botonPrincipal: {
    backgroundColor: '#1B3A6B',
    paddingVertical: 16,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 10,
    shadowColor: '#1B3A6B',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 5,
  },
  botonDeshabilitado: {
    opacity: 0.7,
  },
  botonTexto: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 0.5,
  },
  pie: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 32,
  },
  pieTexto: {
    color: '#718096',
    fontSize: 14,
  },
  enlace: {
    color: '#1B3A6B',
    fontSize: 14,
    fontWeight: '700',
  },
});
