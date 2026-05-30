import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  ScrollView, Alert, ActivityIndicator, StatusBar, Switch,
} from 'react-native';
import { api } from '../api/config';

// HU01: crear una sesion como instructor
export default function CrearSesionScreen({ navigation }: any) {
  const [titulo, setTitulo]           = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [ubicacion, setUbicacion]     = useState('');
  const [duracion, setDuracion]       = useState('');
  const [capacidad, setCapacidad]     = useState('');
  const [fechaSesion, setFecha]       = useState('');
  const [esPrivada, setEsPrivada]     = useState(false);
  const [loading, setLoading]         = useState(false);

  const handleCrear = async () => {
    if (!titulo.trim()) {
      Alert.alert('Campo requerido', 'El titulo de la sesion es obligatorio.');
      return;
    }

    setLoading(true);
    try {
      const fechaBase = fechaSesion
        ? new Date(`${fechaSesion}T18:00:00`).toISOString()
        : new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString();

      const payload = {
        titulo: titulo.trim(),
        descripcion: descripcion.trim(),
        fechaSesion: fechaBase,
        modalidad: ubicacion.trim().toLowerCase().includes('aula') ? 'PRESENCIAL' : 'VIRTUAL',
        maxParticipantes: capacidad ? parseInt(capacidad, 10) : 20,
        tipo: esPrivada ? 'PRIVADA' : 'PUBLICA',
        privada: esPrivada,
        linkSesion: ubicacion.trim(),
        lugar: ubicacion.trim(),
      };

      const resp = await api.post('/sesiones', payload);
      if (resp.data.ok) {
        Alert.alert(
          'Sesion creada',
          'Tu sesion fue creada y queda pendiente de aprobacion.',
          [{ text: 'OK', onPress: () => navigation.goBack() }]
        );
      } else {
        Alert.alert('Error', resp.data.mensaje || 'No se pudo crear la sesion.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'Error al crear la sesion.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1B3A6B" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Cancelar</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Nueva Sesion</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.campo}>
          <Text style={styles.label}>Titulo *</Text>
          <TextInput
            style={styles.input}
            placeholder="Ej: Python para principiantes"
            placeholderTextColor="#B0B8C1"
            value={titulo}
            onChangeText={setTitulo}
          />
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Descripcion</Text>
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="Describe de que trata la sesion..."
            placeholderTextColor="#B0B8C1"
            value={descripcion}
            onChangeText={setDescripcion}
            multiline
            numberOfLines={4}
          />
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Ubicacion / Enlace</Text>
          <TextInput
            style={styles.input}
            placeholder="Ej: Aula 302 o meet.google.com/..."
            placeholderTextColor="#B0B8C1"
            value={ubicacion}
            onChangeText={setUbicacion}
          />
        </View>

        <View style={styles.row}>
          <View style={[styles.campo, { flex: 1, marginRight: 8 }]}>
            <Text style={styles.label}>Duracion (min)</Text>
            <TextInput
              style={styles.input}
              placeholder="60"
              placeholderTextColor="#B0B8C1"
              value={duracion}
              onChangeText={setDuracion}
              keyboardType="number-pad"
            />
          </View>
          <View style={[styles.campo, { flex: 1 }]}>
            <Text style={styles.label}>Capacidad max.</Text>
            <TextInput
              style={styles.input}
              placeholder="20"
              placeholderTextColor="#B0B8C1"
              value={capacidad}
              onChangeText={setCapacidad}
              keyboardType="number-pad"
            />
          </View>
        </View>

        <View style={styles.campo}>
          <Text style={styles.label}>Fecha (AAAA-MM-DD)</Text>
          <TextInput
            style={styles.input}
            placeholder="2026-06-15"
            placeholderTextColor="#B0B8C1"
            value={fechaSesion}
            onChangeText={setFecha}
          />
        </View>

        <View style={styles.toggleRow}>
          <View>
            <Text style={styles.label}>Sesion privada</Text>
            <Text style={styles.toggleHint}>Requiere invitacion para unirse</Text>
          </View>
          <Switch
            value={esPrivada}
            onValueChange={setEsPrivada}
            trackColor={{ false: '#CBD5E0', true: '#1B3A6B' }}
            thumbColor="#FFFFFF"
          />
        </View>

        <TouchableOpacity style={[styles.boton, loading && { opacity: 0.7 }]} onPress={handleCrear} disabled={loading}>
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonTexto}>Crear Sesion</Text>}
        </TouchableOpacity>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7F9FC' },
  header: { backgroundColor: '#1B3A6B', paddingTop: 48, paddingBottom: 16, paddingHorizontal: 20 },
  backTexto: { color: '#C8D8F0', fontSize: 14, marginBottom: 6 },
  headerTitulo: { color: '#FFFFFF', fontSize: 20, fontWeight: '700' },
  scroll: { padding: 20, paddingBottom: 40 },
  campo: { marginBottom: 18 },
  label: { fontSize: 13, fontWeight: '600', color: '#4A5568', marginBottom: 8 },
  input: { backgroundColor: '#FFFFFF', borderWidth: 1.5, borderColor: '#E2E8F0', borderRadius: 10, paddingVertical: 13, paddingHorizontal: 16, fontSize: 15, color: '#1A202C' },
  textArea: { minHeight: 100, textAlignVertical: 'top' },
  row: { flexDirection: 'row' },
  toggleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#FFFFFF', borderRadius: 12, padding: 16, marginBottom: 24, borderWidth: 1.5, borderColor: '#E2E8F0' },
  toggleHint: { fontSize: 12, color: '#718096', marginTop: 2 },
  boton: { backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center', elevation: 5 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
