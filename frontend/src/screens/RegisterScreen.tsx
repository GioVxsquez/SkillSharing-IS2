import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  Alert, ActivityIndicator, KeyboardAvoidingView, Platform,
  ScrollView, StatusBar,
} from 'react-native';
import { api } from '../api/config';

export default function RegisterScreen({ navigation }: any) {
  const [nombre, setNombre]               = useState('');
  const [email, setEmail]                 = useState('');
  const [password, setPassword]           = useState('');
  const [confirmPassword, setConfirm]     = useState('');
  const [isInstructor, setIsInstructor]   = useState(false);
  const [loading, setLoading]             = useState(false);

  const handleRegistro = async () => {
    if (!nombre || !email || !password || !confirmPassword) {
      Alert.alert('Faltan datos', 'Completa todos los campos para continuar.');
      return;
    }
    if (password !== confirmPassword) {
      Alert.alert('Error', 'Las contraseñas no coinciden.');
      return;
    }
    if (password.length < 6) {
      Alert.alert('Error', 'La contraseña debe tener al menos 6 caracteres.');
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
          'Cuenta creada',
          response.data.mensaje || 'Revisa tu correo para activar la cuenta.',
          [{ text: 'Iniciar sesión', onPress: () => navigation.navigate('Login') }]
        );
      } else {
        Alert.alert('Error', response.data.mensaje || 'No se pudo completar el registro.');
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

        {/* Bloque superior */}
        <View style={styles.topBlock}>
          <View style={styles.logoBadge}>
            <Text style={styles.logoBadgeLetra}>S</Text>
          </View>
          <Text style={styles.appNombre}>SkillSharing</Text>
          <Text style={styles.appSlogan}>Empieza a compartir lo que sabes</Text>
        </View>

        {/* Formulario */}
        <View style={styles.formCard}>
          <Text style={styles.formTitulo}>Crea tu cuenta</Text>
          <Text style={styles.formSubtitulo}>Es gratis, tarda 30 segundos</Text>

          {/* Selector de rol */}
          <View style={styles.rolRow}>
            <TouchableOpacity
              style={[styles.rolBtn, !isInstructor && styles.rolBtnActivo]}
              onPress={() => setIsInstructor(false)}
            >
              <Text style={[styles.rolTexto, !isInstructor && styles.rolTextoActivo]}>Aprendiz</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.rolBtn, isInstructor && styles.rolBtnActivo]}
              onPress={() => setIsInstructor(true)}
            >
              <Text style={[styles.rolTexto, isInstructor && styles.rolTextoActivo]}>Instructor</Text>
            </TouchableOpacity>
          </View>

          <View style={styles.campo}>
            <Text style={styles.label}>Nombre</Text>
            <TextInput
              style={styles.input}
              placeholder="Tu nombre completo"
              placeholderTextColor="#8898AA"
              value={nombre}
              onChangeText={setNombre}
            />
          </View>

          <View style={styles.campo}>
            <Text style={styles.label}>Correo</Text>
            <TextInput
              style={styles.input}
              placeholder="tu@correo.com"
              placeholderTextColor="#8898AA"
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
              placeholder="Mínimo 6 caracteres"
              placeholderTextColor="#8898AA"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
            />
          </View>

          <View style={styles.campo}>
            <Text style={styles.label}>Repite la contraseña</Text>
            <TextInput
              style={styles.input}
              placeholder="••••••••"
              placeholderTextColor="#8898AA"
              value={confirmPassword}
              onChangeText={setConfirm}
              secureTextEntry
            />
          </View>

          <TouchableOpacity
            style={[styles.boton, loading && styles.botonDeshabilitado]}
            onPress={handleRegistro}
            disabled={loading}
            activeOpacity={0.85}
          >
            {loading
              ? <ActivityIndicator color="#fff" />
              : <Text style={styles.botonTexto}>Crear cuenta</Text>}
          </TouchableOpacity>

          <View style={styles.pie}>
            <Text style={styles.pieTexto}>¿Ya tienes cuenta? </Text>
            <TouchableOpacity onPress={() => navigation.navigate('Login')}>
              <Text style={styles.enlace}>Inicia sesión</Text>
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
    paddingTop: 60,
    paddingBottom: 30,
  },
  logoBadge: {
    width: 64, height: 64, borderRadius: 20,
    backgroundColor: '#F97316',
    justifyContent: 'center', alignItems: 'center',
    marginBottom: 14,
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.5, shadowRadius: 14, elevation: 10,
  },
  logoBadgeLetra: { color: '#FFFFFF', fontSize: 30, fontWeight: '900' },
  appNombre: { color: '#FFFFFF', fontSize: 24, fontWeight: '800', letterSpacing: 1 },
  appSlogan: { color: '#8898AA', fontSize: 13, marginTop: 6 },
  formCard: {
    flex: 1, backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 32, borderTopRightRadius: 32,
    paddingHorizontal: 28, paddingTop: 32, paddingBottom: 40,
  },
  formTitulo: { fontSize: 22, fontWeight: '800', color: '#0F1C36', marginBottom: 4 },
  formSubtitulo: { fontSize: 14, color: '#8898AA', marginBottom: 20 },
  rolRow: {
    flexDirection: 'row', gap: 10, marginBottom: 20,
    backgroundColor: '#F0F4F8', padding: 4, borderRadius: 14,
  },
  rolBtn: {
    flex: 1, paddingVertical: 10, borderRadius: 11, alignItems: 'center',
  },
  rolBtnActivo: { backgroundColor: '#0F1C36' },
  rolTexto: { fontSize: 14, fontWeight: '700', color: '#8898AA' },
  rolTextoActivo: { color: '#FFFFFF' },
  campo: { marginBottom: 16 },
  label: { fontSize: 12, fontWeight: '700', color: '#4A5568', marginBottom: 7, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: {
    backgroundColor: '#F7F9FC', borderWidth: 1.5, borderColor: '#E2E8F0',
    borderRadius: 12, paddingVertical: 13, paddingHorizontal: 16, fontSize: 15, color: '#0F1C36',
  },
  boton: {
    backgroundColor: '#F97316', paddingVertical: 15, borderRadius: 14,
    alignItems: 'center', marginTop: 8,
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.35, shadowRadius: 10, elevation: 6,
  },
  botonDeshabilitado: { opacity: 0.7 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
  pie: { flexDirection: 'row', justifyContent: 'center', marginTop: 24 },
  pieTexto: { color: '#718096', fontSize: 14 },
  enlace: { color: '#F97316', fontSize: 14, fontWeight: '700' },
});
