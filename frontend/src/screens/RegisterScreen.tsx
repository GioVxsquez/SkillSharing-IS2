import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  Alert, ActivityIndicator, KeyboardAvoidingView, Platform,
  ScrollView, Image, StatusBar,
} from 'react-native';
import { api } from '../api/config';

export default function RegisterScreen({ navigation }: any) {
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirm] = useState('');
  const [isInstructor, setIsInstructor] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleRegistro = async () => {
    if (!nombre || !email || !password || !confirmPassword) {
      Alert.alert('Campos incompletos', 'Por favor completa todos los campos.');
      return;
    }
    if (password !== confirmPassword) {
      Alert.alert('Error', 'Las contrasenas no coinciden.');
      return;
    }
    if (password.length < 6) {
      Alert.alert('Error', 'La contrasena debe tener al menos 6 caracteres.');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/auth/registro', {
        nombre,
        email,
        password,
        rol: isInstructor ? 'INSTRUCTOR' : 'APRENDIZ',
      });

      if (response.data.ok) {
        Alert.alert(
          'Registro exitoso',
          response.data.mensaje || 'Revisa tu correo para activar la cuenta.',
          [{ text: 'Entendido', onPress: () => navigation.navigate('Login') }]
        );
      } else {
        Alert.alert('Error', response.data.mensaje || 'No se pudo completar el registro.');
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
        <View style={styles.logoContainer}>
          <Image source={require('../../assets/logo.png')} style={styles.logo} resizeMode="contain" />
        </View>

        <Text style={styles.titulo}>Crea tu Cuenta</Text>

        <View style={styles.campo}>
          <Text style={styles.label}>Nombre completo</Text>
          <TextInput
            style={styles.input}
            placeholder="Juan Perez"
            placeholderTextColor="#B0B8C1"
            value={nombre}
            onChangeText={setNombre}
          />
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Correo electronico</Text>
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
          <Text style={styles.label}>Contrasena</Text>
          <TextInput
            style={styles.input}
            placeholder="********"
            placeholderTextColor="#B0B8C1"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Confirmar contrasena</Text>
          <TextInput
            style={styles.input}
            placeholder="********"
            placeholderTextColor="#B0B8C1"
            value={confirmPassword}
            onChangeText={setConfirm}
            secureTextEntry
          />
        </View>

        <View style={styles.rolContainer}>
          <Text style={styles.label}>Quiero registrarme como:</Text>
          <View style={styles.rolBotones}>
            <TouchableOpacity
              style={[styles.rolBtn, !isInstructor && styles.rolBtnActivo]}
              onPress={() => setIsInstructor(false)}
            >
              <Text style={[styles.rolTexto, !isInstructor && styles.rolTextoActivo]}>
                Aprendiz
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.rolBtn, isInstructor && styles.rolBtnActivo]}
              onPress={() => setIsInstructor(true)}
            >
              <Text style={[styles.rolTexto, isInstructor && styles.rolTextoActivo]}>
                Instructor
              </Text>
            </TouchableOpacity>
          </View>
        </View>

        <TouchableOpacity
          style={[styles.botonPrincipal, loading && styles.botonDeshabilitado]}
          onPress={handleRegistro}
          disabled={loading}
          activeOpacity={0.85}
        >
          {loading
            ? <ActivityIndicator color="#fff" />
            : <Text style={styles.botonTexto}>Registrarme</Text>}
        </TouchableOpacity>

        <View style={styles.pie}>
          <Text style={styles.pieTexto}>Ya tienes cuenta? </Text>
          <TouchableOpacity onPress={() => navigation.navigate('Login')}>
            <Text style={styles.enlace}>Inicia sesion</Text>
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
    marginBottom: 24,
  },
  logo: {
    width: 180,
    height: 60,
  },
  titulo: {
    fontSize: 24,
    fontWeight: '700',
    color: '#1B3A6B',
    marginBottom: 24,
    textAlign: 'center',
  },
  campo: {
    marginBottom: 16,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#4A5568',
    marginBottom: 7,
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
  rolContainer: {
    marginBottom: 22,
  },
  rolBotones: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 4,
  },
  rolBtn: {
    flex: 1,
    paddingVertical: 13,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 10,
    backgroundColor: '#F7F9FC',
    alignItems: 'center',
  },
  rolBtnActivo: {
    backgroundColor: '#1B3A6B',
    borderColor: '#1B3A6B',
  },
  rolTexto: {
    fontSize: 14,
    fontWeight: '600',
    color: '#718096',
  },
  rolTextoActivo: {
    color: '#FFFFFF',
  },
  botonPrincipal: {
    backgroundColor: '#1B3A6B',
    paddingVertical: 16,
    borderRadius: 10,
    alignItems: 'center',
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
    marginTop: 28,
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
