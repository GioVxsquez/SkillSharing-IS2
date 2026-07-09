import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  ScrollView, Alert, ActivityIndicator, StatusBar, Switch,
} from 'react-native';
import { api } from '../api/config';

// HU01, US09
export default function CrearSesionScreen({ navigation }: any) {
  const [titulo, setTitulo]         = useState('');
  const [descripcion, setDesc]      = useState('');
  const [lugar, setLugar]           = useState('');
  const [capacidad, setCapacidad]   = useState('');
  const [fechaSesion, setFecha]     = useState('');
  const [esPrivada, setEsPrivada]   = useState(false);
  const [categoria, setCategoria]   = useState('Programacion');
  const [modalidad, setModalidad]   = useState<'VIRTUAL' | 'PRESENCIAL'>('VIRTUAL');
  const [loading, setLoading]       = useState(false);

  const categorias = [
    'Programacion', 'Idiomas', 'Cocina', 'Diseno Grafico',
    'Matematicas', 'Musica', 'Fotografia', 'Marketing Digital', 'Finanzas',
  ];

  const handleCrear = async () => {
    if (!titulo.trim() || !fechaSesion.trim()) {
      Alert.alert('Faltan datos', 'El título y la fecha son obligatorios.');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        titulo:          titulo.trim(),
        descripcion:     descripcion.trim(),
        fechaSesion:     new Date(`${fechaSesion}T18:00:00`).toISOString(),
        modalidad:       modalidad,
        maxParticipantes: capacidad ? parseInt(capacidad, 10) : 20,
        tipo:            esPrivada ? 'PRIVADA' : 'PUBLICA',
        privada:         esPrivada,
        linkSesion:      modalidad === 'VIRTUAL'     ? lugar.trim() : null,
        lugar:           modalidad === 'PRESENCIAL'  ? lugar.trim() : null,
        categoria:       categoria,
      };

      const resp = await api.post('/sesiones', payload);
      if (resp.data.ok) {
        Alert.alert('Sesión creada', 'Tu sesión se publicó con éxito.', [{ text: 'OK', onPress: () => navigation.goBack() }]);
      } else {
        Alert.alert('Aviso', resp.data.mensaje || 'No se pudo crear la sesión.');
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'Hubo un error al crear la sesión.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Cancelar</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitulo}>Nueva sesión</Text>
        <View style={{ width: 50 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
        <View style={styles.card}>
          {/* Título */}
          <View style={styles.campo}>
            <Text style={styles.label}>Título *</Text>
            <TextInput
              style={styles.input}
              placeholder="Ej: Taller intensivo de React"
              placeholderTextColor="#8898AA"
              value={titulo}
              onChangeText={setTitulo}
            />
          </View>

          {/* Descripción */}
          <View style={styles.campo}>
            <Text style={styles.label}>Descripción</Text>
            <TextInput
              style={[styles.input, styles.textArea]}
              placeholder="¿Qué aprenderán en esta sesión?"
              placeholderTextColor="#8898AA"
              value={descripcion}
              onChangeText={setDesc}
              multiline
              numberOfLines={4}
            />
          </View>

          {/* Categoría */}
          <View style={styles.campo}>
            <Text style={styles.label}>Categoría</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.chips}>
              {categorias.map((cat) => (
                <TouchableOpacity
                  key={cat}
                  style={[styles.chip, categoria === cat && styles.chipActivo]}
                  onPress={() => setCategoria(cat)}
                >
                  <Text style={[styles.chipTexto, categoria === cat && styles.chipTextoActivo]}>{cat}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>

          {/* Modalidad */}
          <View style={styles.campo}>
            <Text style={styles.label}>Modalidad</Text>
            <View style={styles.modalidadRow}>
              <TouchableOpacity
                style={[styles.modalidadBtn, modalidad === 'VIRTUAL' && styles.modalidadBtnActivo]}
                onPress={() => setModalidad('VIRTUAL')}
              >
                <Text style={[styles.modalidadBtnTexto, modalidad === 'VIRTUAL' && styles.modalidadBtnTextoActivo]}>Virtual</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalidadBtn, modalidad === 'PRESENCIAL' && styles.modalidadBtnActivo]}
                onPress={() => setModalidad('PRESENCIAL')}
              >
                <Text style={[styles.modalidadBtnTexto, modalidad === 'PRESENCIAL' && styles.modalidadBtnTextoActivo]}>Presencial</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Link o lugar */}
          <View style={styles.campo}>
            <Text style={styles.label}>{modalidad === 'VIRTUAL' ? 'Enlace (Zoom, Meet)' : 'Lugar o Aula'}</Text>
            <TextInput
              style={styles.input}
              placeholder={modalidad === 'VIRTUAL' ? 'meet.google.com/...' : 'Aula 302, Pabellón B'}
              placeholderTextColor="#8898AA"
              value={lugar}
              onChangeText={setLugar}
            />
          </View>

          {/* Fecha y Capacidad */}
          <View style={styles.row}>
            <View style={[styles.campo, { flex: 1, marginRight: 10 }]}>
              <Text style={styles.label}>Fecha (AAAA-MM-DD)</Text>
              <TextInput
                style={styles.input}
                placeholder="2026-10-15"
                placeholderTextColor="#8898AA"
                value={fechaSesion}
                onChangeText={setFecha}
              />
            </View>
            <View style={[styles.campo, { flex: 1 }]}>
              <Text style={styles.label}>Capacidad máx.</Text>
              <TextInput
                style={styles.input}
                placeholder="20"
                placeholderTextColor="#8898AA"
                value={capacidad}
                onChangeText={setCapacidad}
                keyboardType="number-pad"
              />
            </View>
          </View>

          {/* Toggle Privada */}
          <View style={styles.toggleRow}>
            <View style={{ flex: 1 }}>
              <Text style={styles.toggleTitulo}>Sesión privada</Text>
              <Text style={styles.toggleHint}>Las sesiones privadas no aparecen en el buscador. Tendrás que invitar a los alumnos manualmente.</Text>
            </View>
            <Switch
              value={esPrivada}
              onValueChange={setEsPrivada}
              trackColor={{ false: '#E2E8F0', true: '#F97316' }}
              thumbColor="#FFFFFF"
            />
          </View>

          <TouchableOpacity
            style={[styles.boton, loading && { opacity: 0.7 }]}
            onPress={handleCrear}
            disabled={loading}
          >
            {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.botonTexto}>Publicar sesión</Text>}
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  header: {
    backgroundColor: '#0F1C36',
    paddingTop: 52, paddingBottom: 20, paddingHorizontal: 20,
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
  },
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600' },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  scroll: { padding: 16, paddingBottom: 40 },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 16, padding: 20,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.05, shadowRadius: 8, elevation: 2,
  },
  campo: { marginBottom: 20 },
  label: { fontSize: 12, fontWeight: '700', color: '#4A5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: 0.5 },
  input: {
    backgroundColor: '#F7F9FC', borderWidth: 1.5, borderColor: '#E2E8F0',
    borderRadius: 12, paddingVertical: 14, paddingHorizontal: 16, fontSize: 15, color: '#0F1C36',
  },
  textArea: { minHeight: 110, textAlignVertical: 'top' },
  row: { flexDirection: 'row' },
  chips: { gap: 8, paddingVertical: 4 },
  chip: {
    paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
    backgroundColor: '#F0F4F8', borderWidth: 1.5, borderColor: '#E2E8F0',
  },
  chipActivo: { backgroundColor: '#F97316', borderColor: '#F97316' },
  chipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  chipTextoActivo: { color: '#FFFFFF', fontWeight: '700' },
  modalidadRow: { flexDirection: 'row', gap: 10 },
  modalidadBtn: {
    flex: 1, paddingVertical: 13, borderRadius: 12, borderWidth: 1.5, borderColor: '#E2E8F0',
    alignItems: 'center', backgroundColor: '#F7F9FC',
  },
  modalidadBtnActivo: { borderColor: '#0F1C36', backgroundColor: '#0F1C36' },
  modalidadBtnTexto: { fontSize: 14, fontWeight: '700', color: '#8898AA' },
  modalidadBtnTextoActivo: { color: '#FFFFFF' },
  toggleRow: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    backgroundColor: '#FFF8F4', borderRadius: 14, padding: 16, marginBottom: 24,
    borderWidth: 1.5, borderColor: '#FEEBC8',
  },
  toggleTitulo: { fontSize: 14, fontWeight: '800', color: '#0F1C36', marginBottom: 4 },
  toggleHint: { fontSize: 12, color: '#92400E', lineHeight: 18, paddingRight: 10 },
  boton: {
    backgroundColor: '#F97316', paddingVertical: 16, borderRadius: 14, alignItems: 'center',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.35, shadowRadius: 10, elevation: 6,
  },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '800' },
});
