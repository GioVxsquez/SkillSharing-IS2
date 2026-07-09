import React, { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, StyleSheet,
  ScrollView, Alert, ActivityIndicator, StatusBar, Switch,
} from 'react-native';
import { api } from '../api/config';

// HU01: crear una sesion como instructor
// US09: elegir categoria al crear la sesion
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
    if (!titulo.trim()) {
      Alert.alert('Campo requerido', 'El titulo de la sesion es obligatorio.');
      return;
    }
    if (!fechaSesion.trim()) {
      Alert.alert('Campo requerido', 'La fecha de la sesion es obligatoria.');
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
        Alert.alert(
          'Sesion creada',
          'Tu sesion fue creada exitosamente.',
          [{ text: 'OK', onPress: () => navigation.goBack() }]
        );
      } else {
        Alert.alert('Error', resp.data.mensaje || 'No se pudo crear la sesion.');
      }
    } catch (error: any) {
      // US25: si hay cruce de horario el backend devuelve el mensaje
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

        {/* Titulo */}
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

        {/* Descripcion */}
        <View style={styles.campo}>
          <Text style={styles.label}>Descripcion</Text>
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="Describe de que trata la sesion..."
            placeholderTextColor="#B0B8C1"
            value={descripcion}
            onChangeText={setDesc}
            multiline
            numberOfLines={4}
          />
        </View>

        {/* Categoria - US09 */}
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

        {/* Modalidad - US11 */}
        <View style={styles.campo}>
          <Text style={styles.label}>Modalidad</Text>
          <View style={styles.modalidadRow}>
            <TouchableOpacity
              style={[styles.modalidadBtn, modalidad === 'VIRTUAL' && styles.modalidadBtnActivo]}
              onPress={() => setModalidad('VIRTUAL')}
            >
              <Text style={[styles.modalidadBtnTexto, modalidad === 'VIRTUAL' && styles.modalidadBtnTextoActivo]}>🖥️ Virtual</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.modalidadBtn, modalidad === 'PRESENCIAL' && styles.modalidadBtnActivo]}
              onPress={() => setModalidad('PRESENCIAL')}
            >
              <Text style={[styles.modalidadBtnTexto, modalidad === 'PRESENCIAL' && styles.modalidadBtnTextoActivo]}>🏫 Presencial</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Link o lugar segun modalidad */}
        <View style={styles.campo}>
          <Text style={styles.label}>{modalidad === 'VIRTUAL' ? 'Enlace de la sesion' : 'Lugar / Aula'}</Text>
          <TextInput
            style={styles.input}
            placeholder={modalidad === 'VIRTUAL' ? 'meet.google.com/...' : 'Ej: Aula 302, Piso 3'}
            placeholderTextColor="#B0B8C1"
            value={lugar}
            onChangeText={setLugar}
          />
        </View>

        {/* Fecha y capacidad */}
        <View style={styles.row}>
          <View style={[styles.campo, { flex: 1, marginRight: 8 }]}>
            <Text style={styles.label}>Fecha (AAAA-MM-DD) *</Text>
            <TextInput
              style={styles.input}
              placeholder="2026-08-15"
              placeholderTextColor="#B0B8C1"
              value={fechaSesion}
              onChangeText={setFecha}
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

        {/* Sesion privada */}
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
  chips: { gap: 8, paddingVertical: 4 },
  chip: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 20, backgroundColor: '#E2E8F0', marginRight: 8 },
  chipActivo: { backgroundColor: '#1B3A6B' },
  chipTexto: { fontSize: 13, color: '#4A5568', fontWeight: '600' },
  chipTextoActivo: { color: '#FFFFFF' },
  modalidadRow: { flexDirection: 'row', gap: 10 },
  modalidadBtn: { flex: 1, paddingVertical: 12, borderRadius: 10, borderWidth: 1.5, borderColor: '#E2E8F0', alignItems: 'center', backgroundColor: '#FFFFFF' },
  modalidadBtnActivo: { borderColor: '#1B3A6B', backgroundColor: '#EBF4FF' },
  modalidadBtnTexto: { fontSize: 14, fontWeight: '600', color: '#718096' },
  modalidadBtnTextoActivo: { color: '#1B3A6B' },
  toggleRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#FFFFFF', borderRadius: 12, padding: 16, marginBottom: 24, borderWidth: 1.5, borderColor: '#E2E8F0' },
  toggleHint: { fontSize: 12, color: '#718096', marginTop: 2 },
  boton: { backgroundColor: '#1B3A6B', paddingVertical: 16, borderRadius: 12, alignItems: 'center', elevation: 5 },
  botonTexto: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
});
