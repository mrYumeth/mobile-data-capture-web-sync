import 'dart:io';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:image_picker/image_picker.dart';

import '../database/local_database_service.dart';
import '../models/master_data_item.dart';

class DataCaptureScreen extends StatefulWidget {
  const DataCaptureScreen({super.key});

  @override
  State<DataCaptureScreen> createState() => _DataCaptureScreenState();
}

class _DataCaptureScreenState extends State<DataCaptureScreen> {
  final _formKey = GlobalKey<FormState>();
  final _descriptionController = TextEditingController();

  final _databaseService = LocalDatabaseService.instance;
  final _imagePicker = ImagePicker();

  List<MasterDataItem> _customers = [];
  List<MasterDataItem> _locations = [];
  List<MasterDataItem> _categories = [];

  int? _selectedCustomerId;
  int? _selectedLocationId;
  int? _selectedCategoryId;

  double? _latitude;
  double? _longitude;
  String? _imagePath;

  bool _isLoading = true;
  bool _isSaving = false;
  bool _isCapturingLocation = false;
  bool _isCapturingImage = false;

  @override
  void initState() {
    super.initState();
    _loadMasterData();
  }

  @override
  void dispose() {
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _loadMasterData() async {
    try {
      final customers = await _databaseService.getCustomers();
      final locations = await _databaseService.getLocations();
      final categories = await _databaseService.getCategories();

      if (!mounted) return;

      setState(() {
        _customers = customers;
        _locations = locations;
        _categories = categories;
        _isLoading = false;
      });
    } catch (error) {
      if (!mounted) return;

      setState(() {
        _isLoading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to load master data: $error')),
      );
    }
  }

  Future<void> _captureImage() async {
    setState(() {
      _isCapturingImage = true;
    });

    try {
      final image = await _imagePicker.pickImage(
        source: ImageSource.camera,
        imageQuality: 70,
      );

      if (!mounted) return;

      if (image != null) {
        setState(() {
          _imagePath = image.path;
        });
      }
    } catch (error) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to capture image: $error')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isCapturingImage = false;
        });
      }
    }
  }

  Future<void> _captureLocation() async {
    setState(() {
      _isCapturingLocation = true;
    });

    try {
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();

      if (!serviceEnabled) {
        throw Exception('Location services are disabled.');
      }

      LocationPermission permission = await Geolocator.checkPermission();

      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }

      if (permission == LocationPermission.denied) {
        throw Exception('Location permission denied.');
      }

      if (permission == LocationPermission.deniedForever) {
        throw Exception('Location permission permanently denied.');
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      if (!mounted) return;

      setState(() {
        _latitude = position.latitude;
        _longitude = position.longitude;
      });
    } catch (error) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to capture GPS location: $error')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isCapturingLocation = false;
        });
      }
    }
  }

  Future<void> _saveRecord() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    if (_latitude == null || _longitude == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please capture GPS location before saving.'),
        ),
      );
      return;
    }

    if (_imagePath == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please capture an image before saving.')),
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    try {
      await _databaseService.insertCapturedRecord(
        customerId: _selectedCustomerId!,
        locationId: _selectedLocationId!,
        categoryId: _selectedCategoryId!,
        description: _descriptionController.text.trim(),
        latitude: _latitude!,
        longitude: _longitude!,
        imagePath: _imagePath!,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Record saved locally.')));

      Navigator.of(context).pop();
    } catch (error) {
      if (!mounted) return;

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Failed to save record: $error')));
    } finally {
      if (mounted) {
        setState(() {
          _isSaving = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Capture New Record')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              DropdownButtonFormField<int>(
                value: _selectedCustomerId,
                decoration: const InputDecoration(
                  labelText: 'Customer',
                  border: OutlineInputBorder(),
                ),
                items: _customers.map((customer) {
                  return DropdownMenuItem<int>(
                    value: customer.id,
                    child: Text(customer.name),
                  );
                }).toList(),
                onChanged: (value) {
                  setState(() {
                    _selectedCustomerId = value;
                  });
                },
                validator: (value) {
                  if (value == null) {
                    return 'Customer is required';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<int>(
                value: _selectedLocationId,
                decoration: const InputDecoration(
                  labelText: 'Location',
                  border: OutlineInputBorder(),
                ),
                items: _locations.map((location) {
                  return DropdownMenuItem<int>(
                    value: location.id,
                    child: Text(location.name),
                  );
                }).toList(),
                onChanged: (value) {
                  setState(() {
                    _selectedLocationId = value;
                  });
                },
                validator: (value) {
                  if (value == null) {
                    return 'Location is required';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<int>(
                value: _selectedCategoryId,
                decoration: const InputDecoration(
                  labelText: 'Category',
                  border: OutlineInputBorder(),
                ),
                items: _categories.map((category) {
                  return DropdownMenuItem<int>(
                    value: category.id,
                    child: Text(category.name),
                  );
                }).toList(),
                onChanged: (value) {
                  setState(() {
                    _selectedCategoryId = value;
                  });
                },
                validator: (value) {
                  if (value == null) {
                    return 'Category is required';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descriptionController,
                maxLines: 4,
                decoration: const InputDecoration(
                  labelText: 'Description / Notes',
                  border: OutlineInputBorder(),
                  alignLabelWithHint: true,
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Description is required';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 20),
              _InfoCard(
                title: 'GPS Location',
                content: _latitude == null || _longitude == null
                    ? 'No GPS location captured yet.'
                    : 'Latitude: $_latitude\nLongitude: $_longitude',
                buttonText: _isCapturingLocation
                    ? 'Capturing...'
                    : 'Capture GPS',
                icon: Icons.my_location_outlined,
                onPressed: _isCapturingLocation ? null : _captureLocation,
              ),
              const SizedBox(height: 16),
              _ImageCaptureCard(
                imagePath: _imagePath,
                isLoading: _isCapturingImage,
                onCapture: _captureImage,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: _isSaving ? null : _saveRecord,
                  icon: _isSaving
                      ? const SizedBox(
                          height: 18,
                          width: 18,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.save_outlined),
                  label: Text(_isSaving ? 'Saving...' : 'Save Locally'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  final String title;
  final String content;
  final String buttonText;
  final IconData icon;
  final VoidCallback? onPressed;

  const _InfoCard({
    required this.title,
    required this.content,
    required this.buttonText,
    required this.icon,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Icon(icon),
                const SizedBox(width: 8),
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(content),
            const SizedBox(height: 12),
            OutlinedButton(onPressed: onPressed, child: Text(buttonText)),
          ],
        ),
      ),
    );
  }
}

class _ImageCaptureCard extends StatelessWidget {
  final String? imagePath;
  final bool isLoading;
  final VoidCallback onCapture;

  const _ImageCaptureCard({
    required this.imagePath,
    required this.isLoading,
    required this.onCapture,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Row(
              children: [
                Icon(Icons.camera_alt_outlined),
                SizedBox(width: 8),
                Text(
                  'Captured Image',
                  style: TextStyle(fontWeight: FontWeight.w600),
                ),
              ],
            ),
            const SizedBox(height: 12),
            if (imagePath == null)
              const Text('No image captured yet.')
            else
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.file(
                  File(imagePath!),
                  height: 180,
                  fit: BoxFit.cover,
                ),
              ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: isLoading ? null : onCapture,
              child: Text(isLoading ? 'Opening Camera...' : 'Capture Image'),
            ),
          ],
        ),
      ),
    );
  }
}
